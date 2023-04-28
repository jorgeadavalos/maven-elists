ajaxObjs = [];
ajaxID = null;
function ajaxObj(name) {
	this.ajaxURL	= name;
	this.ajaxDestEl	= document.createElement("div");
	this.ajaxDone	= false;
	this.ajaxError	= false;
	this.ajaxFunc	= null;
	this.status		= -1;
	this.key		= null;
	this.ajaxmsg;
}
function ajaxRequest(obj) {
	if (obj.key == null) obj.key = obj.ajaxURL;
	ajaxObjs.push(obj);
	ajaxServerReq(obj); 
	return false;	
}
function ajaxServerReq(obj) {
	var pageRequest;
	if (window.XMLHttpRequest) pageRequest = new XMLHttpRequest();
	else if (window.ActiveXObject)  pageRequest = new ActiveXObject("Microsfot.XMLHTTP"); 
		 else
			return;
	
	pageRequest.onreadystatechange = function() {
		if (this.readyState == 4 ) {
			obj.ajaxError = false;
			obj.status = this.status;
			if (this.status != 200) obj.ajaxError = true;
			serverGetData(this.responseText,obj);
			if (this.status == 200) removeObjs(obj);
		}
	};
	pageRequest.open('GET',obj.ajaxURL,true);
	pageRequest.send();
}
function serverGetData(msg,obj) {
	obj.ajaxDone  = true;
	obj.ajaxDestEl.innerHTML = msg;
	obj.ajaxmsg = msg;
	if (obj.ajaxFunc != null)
		obj.ajaxFunc(obj);
	if (ajaxID == null) ajaxID = setInterval('ajaxResends()',60000);
}
function ajaxResends() {
	if (ajaxObjs.length == 0) {
		clearInterval(ajaxID);
		ajaxID = null;
	} else {
		for( var i = 0; i < ajaxObjs.length; i++) { 
			ajaxServerReq(ajaxObjs[i]);
		}
	}
}
function removeObjs(obj200) {
	for( var i = 0; i < ajaxObjs.length; i++) { 
	   if ( ajaxObjs[i].key.localeCompare(obj200.key) == 0) {
		   ajaxObjs.splice(i, 1); 
	   }
	}
}