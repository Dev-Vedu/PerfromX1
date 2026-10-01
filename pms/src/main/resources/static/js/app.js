// End-of-session progress prompt. Opens when an employee signs out with open tasks.
document.addEventListener('DOMContentLoaded', function () {
  var dialog = document.getElementById('wrapup');
  var signOutLinks = document.querySelectorAll('[data-signout]');

  if (dialog && dialog.dataset.hasTasks === 'true') {
    signOutLinks.forEach(function (el) {
      el.addEventListener('click', function (e) {
        e.preventDefault();
        if (typeof dialog.showModal === 'function') {
          dialog.showModal();
        } else {
          el.closest('form').submit();
        }
      });
    });
  }

  var skip = document.getElementById('wrapup-skip');
  if (skip) {
    skip.addEventListener('click', function () {
      document.getElementById('signout-form').submit();
    });
  }

  // Live read-out for the rating sliders.
  document.querySelectorAll('input[type=range][data-output]').forEach(function (range) {
    var out = document.getElementById(range.dataset.output);
    if (!out) return;
    var sync = function () { out.textContent = range.value + ' / 5'; };
    range.addEventListener('input', sync);
    sync();
  });

  // Progress slider read-out.
  document.querySelectorAll('input[type=range].progress-range').forEach(function (range) {
    var out = range.parentElement.querySelector('.progress-out');
    if (!out) return;
    var sync = function () { out.textContent = range.value + '%'; };
    range.addEventListener('input', sync);
    sync();
  });
});
