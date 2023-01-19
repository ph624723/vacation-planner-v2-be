function openAddEvent(start, end) {
    //location.href = "/view/events/add?start="+start+"&end="+end;
    location.href = "#dialogCreate";
    //var message = /*[[${message}]]*/ 'default';
    //console.log(message);var message = /*[[${message}]]*/ 'default';
    //console.log(message);
}
function updatePlannerConfig(event){
    var xhr = new XMLHttpRequest();
    xhr.open("POST","/view/events/test",true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(event));
}