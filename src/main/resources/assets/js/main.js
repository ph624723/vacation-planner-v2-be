function showAlert() {
    alert("The button was clicked!");
}
function gotoAbsencesForPerson(value) {
    location.href = "/view/absences?personId=" + value;
}