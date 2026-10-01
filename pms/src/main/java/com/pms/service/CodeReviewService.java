package com.pms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pms.dto.CodeVerdict;
import com.pms.entity.CodeReview;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.repository.CodeReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The code optimization chatbot. Employees paste code before submitting a task and
 * get a verdict plus concrete improvements, ahead of human review by the lead.
 */
@Service
@RequiredArgsConstructor
public class CodeReviewService {

    private static final String SYSTEM = """
            You are a senior engineer doing a first-pass optimization review inside a
            project management tool. Judge only the code you are given.

            Reply with JSON and nothing else, in this exact shape:
            {
              "verdict": "OPTIMIZED" | "NEEDS_WORK" | "UNCLEAR",
              "score": <integer 0-100>,
              "summary": "<one or two sentences in plain language>",
              "findings": [
                {"issue": "<what is inefficient>",
                 "why": "<cost in time, memory or readability>",
                 "fix": "<concrete change>",
                 "severity": "low" | "medium" | "high"}
              ]
            }

            Focus on algorithmic complexity, redundant work inside loops, unnecessary
            allocations, N+1 queries, missing early exits, resource leaks and obvious
            correctness risks. Skip style nitpicks. Use OPTIMIZED only when you would
            approve the code as-is. Keep every string under 220 characters.
            """;

    private final ClaudeClient claude;
    private final CodeReviewRepository repository;

    public CodeReview review(User employee, Task task, String language, String code) {
        CodeVerdict verdict = analyse(language, code);
        return repository.save(CodeReview.builder()
                .employee(employee).task(task)
                .language(language).code(code)
                .verdict(verdict.getVerdict()).score(verdict.getScore())
                .findings(verdict.getFindings())
                .build());
    }

    public List<CodeReview> history(User employee, int limit) {
        return repository.findByEmployeeOrderByCreatedAtDesc(employee, PageRequest.of(0, limit));
    }

    public List<CodeReview> forTask(Task task) {
        return repository.findByTaskOrderByCreatedAtDesc(task);
    }

    private CodeVerdict analyse(String language, String code) {
        JsonNode node = claude.askJson(SYSTEM,
                "Language: " + (language == null ? "unspecified" : language) + "\n\nCode:\n" + code);
        if (node == null) return heuristic(code);

        StringBuilder sb = new StringBuilder();
        String summary = node.path("summary").asText("");
        if (!summary.isBlank()) sb.append(summary).append("\n\n");
        for (JsonNode f : node.path("findings")) {
            sb.append("• [").append(f.path("severity").asText("medium")).append("] ")
              .append(f.path("issue").asText()).append("\n")
              .append("   Why it matters: ").append(f.path("why").asText()).append("\n")
              .append("   Fix: ").append(f.path("fix").asText()).append("\n");
        }
        String verdict = node.path("verdict").asText("UNCLEAR");
        int score = node.path("score").asInt(60);
        return new CodeVerdict(verdict, score, sb.toString().trim());
    }

    /**
     * Offline fallback so the chatbot still answers without an API key.
     * Pattern based, deliberately conservative: it never claims code is optimized,
     * it only points at common cost sinks.
     */
    private CodeVerdict heuristic(String code) {
        List<String> notes = new ArrayList<>();
        String lower = code.toLowerCase();

        long loopDepth = nestedLoopDepth(code);
        if (loopDepth >= 2) {
            notes.add("• [high] Nested loops detected (depth " + loopDepth + ").\n"
                    + "   Why it matters: runtime grows quadratically or worse with input size.\n"
                    + "   Fix: precompute a map or set for the inner lookup, or flatten the traversal.");
        }
        if (lower.contains("select ") && (lower.contains("for (") || lower.contains("foreach"))) {
            notes.add("• [high] A query appears inside a loop.\n"
                    + "   Why it matters: this is the N+1 pattern; one round trip per row.\n"
                    + "   Fix: fetch the whole set once with a join or an IN clause.");
        }
        if (code.contains("+=") && (code.contains("String ") || lower.contains("str +"))) {
            notes.add("• [medium] String concatenation in a loop.\n"
                    + "   Why it matters: each pass allocates a new string.\n"
                    + "   Fix: use StringBuilder, or join once at the end.");
        }
        if (lower.contains("new ") && loopDepth >= 1) {
            notes.add("• [medium] Objects allocated inside a loop.\n"
                    + "   Why it matters: avoidable pressure on the garbage collector.\n"
                    + "   Fix: hoist anything loop-invariant above the loop.");
        }
        if (lower.contains("contains(") && lower.contains("list")) {
            notes.add("• [medium] Membership test against a List.\n"
                    + "   Why it matters: contains on a List scans every element.\n"
                    + "   Fix: switch the lookup structure to a HashSet.");
        }
        if ((lower.contains("fileinputstream") || lower.contains("new scanner"))
                && !lower.contains("try (")) {
            notes.add("• [high] A resource is opened without try-with-resources.\n"
                    + "   Why it matters: the handle leaks when an exception is thrown.\n"
                    + "   Fix: wrap it in try (var in = ...) { ... }.");
        }

        if (notes.isEmpty()) {
            return new CodeVerdict("UNCLEAR", 60,
                    "No Claude API key is configured, so this is a local pattern check only. "
                    + "Nothing obviously expensive turned up, but treat this as a smoke test rather "
                    + "than a real review. Set CLAUDE_API_KEY to get a full analysis.");
        }
        int score = Math.max(25, 85 - notes.size() * 12);
        return new CodeVerdict("NEEDS_WORK", score,
                "Local pattern check found " + notes.size() + " thing"
                + (notes.size() == 1 ? "" : "s") + " worth changing. "
                + "Set CLAUDE_API_KEY for a full review.\n\n" + String.join("\n", notes));
    }

    private long nestedLoopDepth(String code) {
        int depth = 0, max = 0;
        String[] lines = code.split("\n");
        for (String line : lines) {
            String t = line.trim();
            boolean loop = t.startsWith("for") || t.startsWith("while")
                    || t.startsWith("} for") || t.contains(".forEach");
            if (loop) { depth++; max = Math.max(max, depth); }
            if (t.startsWith("}") && depth > 0) depth--;
        }
        return max;
    }
}
