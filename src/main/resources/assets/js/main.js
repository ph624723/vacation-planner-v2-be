function showAlert() {
    alert("The button was clicked!");
}
function gotoAbsencesForPerson(value) {
    location.href = "/view/absences?personId=" + value;
}
function openLogin(redirect) {
    location.href = "/view/login?redirect=" + redirect;
}