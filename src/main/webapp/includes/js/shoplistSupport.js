function showNav(navName) {
	var el = document.getElementById("navigationframe");
	if (el == null) {
		if (navName == null) navName = "mainNavigation.xhtml";
		var el2 = document.createElement("div"); 
		el2.id = "dynamicDIV";
		document.body.appendChild(el2);
		ajaxDestEl = el2;
		var obj = new ajaxObj(navName);
		obj.ajaxFunc = function(objResp) {
			el2.innerHTML = objResp.ajaxmsg;
			fadeIn();
		}
		ajaxRequest(obj);
	}
	$("nav").toggleClass("active-nav");
} 
function fadeOut() {	
	if (document.getElementById('idFadeOut'))
		document.getElementById('idFadeOut').style.display = 'none';
	
	var el = document.getElementById("idFadeIn");
	el.style.display = 'block';
	el.style.left= "0px";
	$("nav").toggleClass("active-nav");
	return false;	
}
function fadeIn() {	
	if (document.getElementById('idFadeIn'))
		document.getElementById('idFadeIn').style.display = 'none';
	
	var el = document.getElementById("idFadeOut");
	el.style.display = 'block';
	el.style.left= "0px";
	$("nav").toggleClass("active-nav");
	return false;
}
function findElement(nam) {
	var namVar = document.getElementById(nam);
	if (namVar) return namVar;
	for (var i=0;i<document.forms.length;i++) {
		namVar = document.getElementById(document.forms[i].id+":"+nam);
		if (namVar) return namVar;
	}
	var elements = document.getElementsByTagName('*');
	if (elements) {
		for (var i = 0;i<elements.length;i++) {
			if (elements[i].id.indexOf(nam) != -1) {
				return elements[i];
			}
		}
	}
	return null;
}
function setEmailCookie(parm) {
	document.cookie = "email="+parm.value+";SameSite=Strict;";
}