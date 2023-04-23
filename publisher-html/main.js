
window.onload = function() {
	byId("submit").addEventListener("click", search);
	
	byId("text").addEventListener("keypress", function(event) {
		if (event.key === "Enter") {
			event.preventDefault();
			search();
		}
	}); 
	
};

httpGet("http://localhost:8081/events", showEvents);





function search() {
	let searchDTO = {};
	searchDTO.name = byId("text").value;
	searchDTO.fromDate = readDateFromInput("date-from");
	searchDTO.toDate = readDateFromInput("date-to");
	
	httpPost("http://localhost:8081/event/search", searchDTO, showEvents);
}

function readDateFromInput(id){
	let value = byId(id).value;
	if (!value || value === ""){
		return null;
	}
	return new Date(value).toISOString();
}

function showEvents(events) {
	let eventlistTable = byId("eventlist");
	while(eventlistTable.childElementCount > 0 ){
		eventlistTable.removeChild(eventlistTable.lastChild);
	}
	for(let event of events) {
		eventlistTable.appendChild(createEventlistEntry(event));
	}
}

function createEventlistEntry(event) {
	let nameSpan = document.createElement("td");
	nameSpan.innerText = event.name;
	
	let date = new Date(event.startDate);
	let startDateSpan = document.createElement("td");
	startDateSpan.innerText = date.toLocaleString("de");
	

	let labelsSpan = document.createElement("td");
	labelsSpan.innerText = "";
	for(let key in event.data) {
		labelsSpan.innerText += key + ": " + event.data[key] + ", ";
	}
	labelsSpan.innerText = labelsSpan.innerText.substring(0, labelsSpan.innerText.length - 2);
	
	let entryRow = document.createElement("tr");
	entryRow.appendChild(nameSpan);
	entryRow.appendChild(startDateSpan);
	entryRow.appendChild(labelsSpan);
	return entryRow;
}

function httpGet(url, callback)
{
    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(JSON.parse(xmlHttp.responseText));
    }
    xmlHttp.open("GET", url, true);
    xmlHttp.send(null);
}

function httpPost(url, body, callback)
{
    let xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(JSON.parse(xmlHttp.responseText));
    }
    xmlHttp.open("POST", url, true);
	xmlHttp.setRequestHeader("Content-Type", "application/json");
	console.log(body);
    xmlHttp.send(JSON.stringify(body));
}

function byId(id) {
	return document.getElementById(id);
}