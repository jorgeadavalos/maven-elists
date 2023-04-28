fld 		= null;
launchTID	=  null;
var shopTable;
var order = 0;
var partialMaches = [];
var salesObj = [];

var itemNodes	= {};
var deleteNodes = {};
var salesNodes	= {};
var storeNames = [];
function itemNodeTemplate() { 
	this.name			= "";
	this.desc			= "";
	this.selected		= false;
	this.hideit			= false;
	this.temporary		= false;
	this.DBUpdate		= false;
	this.originalorder 	= -1;
	this.buyingorder 	= -1;
	this.photoid 	 	= -1;
	this.familyid 	 	= -1;
	this.category 	 	= "";
	this.stores			= {};
}
function itemNode(JSONObj,store) {
	this.name			= JSONObj.name;
	this.desc			= JSONObj.desc;
	this.selected		= JSONObj.selected;
	this.hideit			= false;
	this.temporary		= JSONObj.temporary;
	this.DBUpdate		= false;
	this.buyingorder 	= JSONObj.buyingorder;
	this.originalorder	= JSONObj.buyingorder;
	this.photoid 		= JSONObj.photoid;
	this.familyid 		= JSONObj.familyid;
	this.category 	 	= JSONObj.category;
	this.stores			= store;
}
function storeNode(name,items) {
	this.name=name;
	this.exist=false;
	this.items=items;
}
function salesNode(name,items) {
	this.store=name;
	this.items=items;
}
function getJSONObj() {
	var json = findElement("jsonobj").innerText;
	var obj = JSON.parse(json, function (key, value) {
		if (key.length > 0) {
			var items = JSON.parse(value);
			for (var  i=0;i<items.length;i++) {
				var storeObj = {};
				if (itemNodes.hasOwnProperty(items[i].name)) {
					storeObj = itemNodes[items[i].name].stores;
				}
				storeObj[key] = new storeNode(key);
				storeObj[key].exist = true;
				itemNodes[items[i].name] = new itemNode(items[i],storeObj);
				if (storeNames.indexOf(key) == -1) storeNames.push(key);
			}
		}
	});
}
document.onclick = function(ev) {
	if (launchTID != null) clearInterval(launchTID);
	launchTID = null;
	cleanSalesInfo();
}

function setMsg(msg,fldName) {
	fld = findElement(fldName); 
	if (fld != null) 
		launchTID = setInterval('launchJNLP("#")',500);
	return false;
}
function launchJNLP(app) {
	if (fld == null) return;
    fld.style.color = fld.style.color == "red" ? "blue" : "red";
} 
function openPage(pageName) {
	var genPopUp = window.open(pageName,"_parent");
 }
function resetopenPage(pageName) {
	var url = "resetFamily.xhtml";
	var obj = new ajaxObj(url);
	obj.ajaxFunc = function(objResp) {
		window.open(pageName,"_parent");
	}
	ajaxRequest(obj); 
 }
function expand(parm,ndx) {
	var el = document.getElementById(parm);
	var plusImg = document.getElementById('plusImg'+ndx);
	var minusImg = document.getElementById('minusImg'+ndx);
	if (el.style.display == "block" || el.style.display == "inherit" ) {
		el.style.display = "none";
		minusImg.style.display = "none";
		plusImg.style.display = "inherit";
	} else {
		el.style.display = "block"; 
		minusImg.style.display = "inherit";
		plusImg.style.display = "none";
	}
}
function removeElement(id) {
	var myNode = findElement(id); 
	if (myNode == null) return;
	while (myNode.firstChild) {
		myNode.removeChild(myNode.firstChild);
	}
	var button1 = document.getElementById("button1");
	if ( button1 != null) document.body.removeChild(button1);
	ShopTable = document.getElementById("shopTable");
	if (shopTable == null) return false;

	for (var i=0;i<shopTable.rows.length;i++) {
		shopTable.deleteRow(0);
	}

}
function removedynElements() {
	removeElement("selectList");
	removeElement("checkboxes");
}
function addCheckbox(obj,div3,collect) {
	var checkbox = document.createElement("input"); 
	checkbox.type ="checkbox";
	checkbox.name = "checkedList";
	checkbox.value = obj.name;
	checkbox.onclick = function() {updateSingleItem(checkbox)};
	checkbox.defaultChecked = obj.selected;
	if (collect) checkbox.onchange = collectElements; 
	var label = document.createElement('label')
	label.appendChild(checkbox);
	label.appendChild(document.createTextNode(obj.name));
	div3.appendChild(label);
}
function hiddenCell(obj,div3) {
	var itemName = document.createElement('input')
	itemName.style.visibility = "hidden"; 
	itemName.name = "itemName";
	itemName.value = obj.name;
	div3.appendChild(itemName);
	div3.style.visibility = "hidden";
}
var prevCategory;
function addRow(mytable,flag,keysSorted) {
	for (var i=0;i<keysSorted.length;i++) {
		var name = keysSorted[i];
		if (itemNodes[name].selected != flag || itemNodes[name].hideit) continue;
	
		var newRow = mytable.insertRow(-1);
		var catetgoryText = itemNodes[name].category;
		if (prevCategory !== itemNodes[name].category) {
			if (catetgoryText.length == 0) catetgoryText = "Other";
			var cell = newRow.insertCell(-1);
			cell.colSpan = "10";
			cell.style.paddingTop = "10px";
			cell.innerHTML = "<div><b>"+catetgoryText+"</b></div>";
			newRow = mytable.insertRow(-1);
			prevCategory = itemNodes[name].category;
		}

		var cell = newRow.insertCell(-1);		
		addCheckbox(itemNodes[name],cell,true);
		cell = newRow.insertCell(-1);
		cell.innerHTML = "<input type='text' size='5' name='descriptions' onfocusout='return updateDescr(this)' value='"+itemNodes[name].desc+"'/>";
		cell = newRow.insertCell(-1);

		if (itemNodes[name].photoid == -1) 
			cell.innerHTML = '<label id="view" value="view" width="20" height="20" >';
		else 
			cell.innerHTML = '<input type="image" id="view" value="view" src="includes/images/downloadArrow.jpg" width="20" height="20" onclick="return displayImage(this)">';

		cell = newRow.insertCell(-1);
		cell.innerHTML = '<input type="image" id="upload" value="upload" src="includes/images/uploadArrow.jpg" width="20" height="20" onclick="return UploadImage(this)">';
		cell = newRow.insertCell(-1);
		//hiddenCell(itemNodes[name],cell);
    }
}
function sortOnName(parmNodes,shopTable,onlyChecked) {
	keysSorted = Object.keys(parmNodes).sort(function(a,b){return parmNodes[a].name.localeCompare(parmNodes[b].name)})
	var cats = findElement("categoryList");
	var selects = [];
	for (var i=1;i<cats.options.length;i++) {
		selects[i] = cats.options[i].text;
	}
	for (var i=1;i<selects.length;i++) {
		var keys = [];
		var selectKey = selects[i];
		for (var j=0;j<keysSorted.length;j++) {
			var key = parmNodes[keysSorted[j]].category.trim();
			if (typeof key === 'undefined' || key.length == 0) key = "Other";
			if (selectKey === key) {
				keys.push(keysSorted[j]);
				continue;
			}
		}
		addRow(shopTable,true,keys); //checked  
		if (!onlyChecked) addRow(shopTable,false,keys); //unchecked 
	}
}
function bldMultipleSelect(htmlTag,parmNodes,flag) {
	removedynElements();
	var div0 = document.getElementById(htmlTag);

	if (div0 == null)  {
		div0 = document.createElement("div"); 
		div0.style.overflow = "auto";
		
		div0.id = "selectList";
		document.body.appendChild(div0);
	}

	var shopTable = document.createElement("TABLE");
	shopTable.id = "shopTable";
	var header = shopTable.createTHead();
	var row = header.insertRow(0);     
	var cell = row.insertCell(-1);
	cell.innerHTML = "<b>Item name </b>";
	var cell = row.insertCell(-1);
	cell.innerHTML = "<b>description </b>";
	var cell = row.insertCell(-1);
	cell.innerHTML = "<b>display<br/>image</b>";
	var cell = row.insertCell(-1);
	cell.innerHTML = "<b>upload<br/>image</b>";
	
	var div3 = document.createElement("div"); 
	div3.id = "checkboxes";
	div3.className="multiselect";

	sortOnName(parmNodes,shopTable,flag);
	div0.appendChild(shopTable);
}
function initShoppingList(store) {
	if (document.querySelector("#trigger") != null)
		document.querySelector("#trigger").addEventListener("click", showSelectedItems);
	document.getElementById("storename").value = store;
	var el=document.getElementById("storeTitle");
	el.innerText = "Full Master list";
	if (store != null && store.length > 0) initStoreList(store);
	else refresh(itemNodes,false); 
}
function initStoreList(store) {
	if (store == null || store.length == 0) store=document.getElementById("storename").value; 
	var el=document.getElementById("storeTitle");
	if (store != null && store.length > 0) el.innerText = "Store name "+store;
	var parm=document.getElementById("storename"); 
	parm.value = store;
	
	if (store == null || store.length == 0) 
		refresh(itemNodes,false);
	else {
		var storeNodes = populateStore(store);
		refresh(storeNodes,false);
		getJsonSales(storeNodes,store);
	}
}
function populateStore(store) {
	var storeNodes = {};
	for (var name in itemNodes) {
		if (itemNodes.hasOwnProperty(name)) {
			if (itemNodes[name].stores[store] == null) continue;
			if (store.localeCompare(itemNodes[name].stores[store].name) != 0)  continue;
			storeNodes[name] = itemNodes[name];
		}
	}
	return storeNodes;
}
function collectcheckedbox() {
	var el=document.getElementsByName("checkedList");
	var desc=document.getElementsByName("descriptions");
	
	if (el.length == 0) return;
	for (var name in itemNodes) {
		if (itemNodes.hasOwnProperty(name)) {
			itemNodes[name].hideit = false;
		}
    }
	for (var i=0;i<el.length;i++) {
		if (el[i].type != "checkbox") continue;
		if (desc[i].value == "null") desc[i].value = "";
		if ((itemNodes[el[i].value].desc != null && itemNodes[el[i].value].desc.localeCompare(desc[i].value)) != 0 || el[i].checked)
			itemNodes[el[i].value].DBUpdate = true;	
		if (itemNodes[el[i].value].selected != el[i].checked)
			itemNodes[el[i].value].DBUpdate = true;	

		itemNodes[el[i].value].desc = desc[i].value;  
		itemNodes[el[i].value].selected = el[i].checked
	}
}
function refresh(parmNodes,flag) {
	collectcheckedbox();
	//parmNodes = itemNodes;
	var el = document.getElementById("chooseItem").value.toUpperCase();
	for (var name in parmNodes) {
		parmNodes[name].hideit = false;
	}
	if (el.length > 0)
		for (var name in parmNodes) {
			if (parmNodes.hasOwnProperty(name)) {
				var cmpNameDesc = name + parmNodes[name].desc;
				if (cmpNameDesc.toUpperCase().indexOf(el) == -1) parmNodes[name].hideit = true;
			}
		}
	bldMultipleSelect("selectList",parmNodes,flag);
	return false;
}
function refreshAdminTbl(parm1,parm2) {
	var el = document.getElementById(parm1).value.toUpperCase();
	for (var name in itemNodes) {
		if (itemNodes.hasOwnProperty(name)) {
			if (itemNodes[name].name.toUpperCase().indexOf(el) == -1 ) 
				itemNodes[name].hideit = true;
			else
				itemNodes[name].hideit = false;
		}
	}
	var adminTable = findElement(parm2);
	var savedRow = adminTable.getElementsByTagName('tr')[0];
	removeElement(parm2);
	adminTable.appendChild(savedRow);
	var keysSorted = Object.keys(itemNodes).sort(function(a,b){return itemNodes[a].name.localeCompare(itemNodes[b].name)});
	for (var i=0;i<keysSorted.length;i++) {
		if (itemNodes[keysSorted[i]].hideit) continue;
		adminRow(adminTable,itemNodes[keysSorted[i]]);			
	}
	return false;
}
function addAdminRow(adminTable) {
	var newRow = adminTable.insertRow(-1);
	var cell = newRow.insertCell(0);
	var itemName = document.createElement("input");
	itemName.name = "itemname";
	cell.appendChild(itemName); 		
	for (var i=1;i<adminTable.rows[0].cells.length;i++) {
		var text = adminTable.rows[0].cells[i].innerText.trim();
		var obj = new itemNodeTemplate();
		obj.name = text;
		cell.noWrap = "noWrap";
		cell = newRow.insertCell(-1);
		addCheckbox(obj,cell);
	}
	cell.noWrap = "noWrap";
}
function collectDeleteElements(itemName,store,checked) {
	if (deleteNodes.hasOwnProperty(itemName)) {
		deleteNodes[itemName].stores[store] = new storeNode(store);
		storesObj = deleteNodes[itemName].stores;
	} else {
		deleteNodes[itemName] = new itemNodeTemplate();
		deleteNodes[itemName].stores[store] = new storeNode(store);
	}
	deleteNodes[itemName].name = itemName;
	deleteNodes[itemName].stores[store].name = store;
	deleteNodes[itemName].stores[store].exist = checked;
}
function collectElements(evt) {
	var trTag = evt.target.parentNode.parentNode.parentNode;
	var itemName = trTag.firstChild.firstChild.value;
	if (itemName != null)
		collectDeleteElements(itemName,evt.currentTarget.value,evt.currentTarget.checked);
	else {
		itemName = evt.currentTarget.value;
		
		if (itemNodes.hasOwnProperty(itemName)) {
			if (!evt.currentTarget.checked)
				itemNodes[itemName].buyingorder = order;
		}
		order = order + 1;
	}
}
function adminRow(adminTable,itemObj,deleteFlag) {
	var newRow = adminTable.insertRow(-1);
	var cell = newRow.insertCell(0);
	var itemName = document.createElement("input");
	itemName.name = "itemname";
	itemName.value = itemObj.name; 
	itemName.readOnly = true; 
	
	cell.appendChild(itemName); 		
	for (var i=1;i<adminTable.rows[0].cells.length;i++) {
		var obj = new itemNodeTemplate();
		obj.name = adminTable.rows[0].cells[i].innerText;
		for (var name in itemObj.stores) {
			if (obj.name == name) {
				obj.selected = itemObj.stores[name].exist;
				break;
			}
		}
		cell = newRow.insertCell(i);
		addCheckbox(obj,cell,true);
		if (!obj.selected && deleteFlag) {
			cell.firstChild.innerText = "";
			continue;
		}
	}
}
/* check if function still used */
function bldItemRow(parm,cats) {
	var adminTable = document.getElementById(parm);
	for (var i=0;i<5;i++) {
		addAdminRow(adminTable);
	}
	var catList = document.getElementById(cats);
	for (var i=0;i<adminTable.rows.length;i++) {
		if (i==0) continue;

		var cell = adminTable.rows[i].insertCell(-1);
		cell.noWrap= "noWrap";
		var dropdown = document.createElement("select");
//		dropdown.onchange= function() {upateCategory(this,cell);};
		dropdown.innerHTML = catList.innerHTML;
		cell.appendChild(dropdown); 		
	}
}
function bldDeleteItems(parm) {
	getJSONObj();
	var adminTable = document.getElementById(parm);
	var keysSorted = Object.keys(itemNodes).sort(function(a,b){return itemNodes[a].name.localeCompare(itemNodes[b].name)});
	for (var i=0;i<keysSorted.length;i++) {
		adminRow(adminTable,itemNodes[keysSorted[i]],true);			
	}
}
function bldUpdateItems(parm) {
	getJSONObj();
	var adminTable = document.getElementById(parm);
	var keysSorted = Object.keys(itemNodes).sort(function(a,b){return itemNodes[a].name.localeCompare(itemNodes[b].name)});
	for (var i=0;i<keysSorted.length;i++) {
		adminRow(adminTable,itemNodes[keysSorted[i]],false);			
	}
}
function bldCategories(parm,cats) {
	getJSONObj();
	var adminTable = document.getElementById(parm);
	var catList = document.getElementById(cats);
	var keysSorted = Object.keys(itemNodes).sort(function(a,b){return itemNodes[a].name.localeCompare(itemNodes[b].name)});
	for (var i=0;i<keysSorted.length;i++) {
		categoryRow(adminTable,itemNodes[keysSorted[i]],catList,adminTable.insertRow(-1));			
	}
}
function upateCategory(catSelected,cell) {
	var row = cell.parentNode;
	var itemValue = row.children[1].children[0].value;
	var newValue = catSelected[catSelected.selectedIndex].value;
	if (itemValue != newValue) {
		row.children[1].children[0].value = newValue;
		var jsonNodes	= [];
		var key = row.children[0].children[0].value;
		itemNodes[key].category = newValue;
		jsonNodes.push(itemNodes[key]);
		updateSingleItem2(jsonNodes);

	}
}
function categoryRow(adminTable,itemObj,cats,newRow) {
	var cell = newRow.insertCell(-1);
	var itemName = document.createElement("input");
	itemName.name = "itemname";
	itemName.value = itemObj.name; 
	itemName.readOnly = true; 
	cell.appendChild(itemName); 
	
	cell = newRow.insertCell(-1);
	itemName = document.createElement("input");
	itemName.name = "category";
	itemName.value = itemObj.category; 
	itemName.readOnly = true; 
	cell.appendChild(itemName); 

	cell = newRow.insertCell(-1);
	var dropdown = document.createElement("select");
	dropdown.onchange= function() {upateCategory(this,cell);};
	dropdown.innerHTML = cats.innerHTML;
	cell.appendChild(dropdown); 		
}
function tableAsJSON(parm,jsonid) {
	var wrkTable = document.getElementById(parm);
	var jsonNodes	= [];
	for (var i=1;i<wrkTable.rows[0].cells.length;i++) {
		var storeName = wrkTable.rows[0].cells[i].innerText;
		var items		= [];
		for (var j=1;j<wrkTable.rows.length;j++) {
			var cell = wrkTable.rows[j].cells[i].firstChild;
			if (cell.firstChild.type != "checkbox" || !cell.firstChild.checked) continue;
			var value = wrkTable.rows[j].cells[0].firstChild.value.trim();
			if (value.length == 0) continue;
			
			var obj = new itemNodeTemplate();
			obj.name = wrkTable.rows[j].cells[0].firstChild.value;
			
			var len = wrkTable.rows[j].cells.length;
			var lastCell = wrkTable.rows[j].cells[len-1];
			var select = lastCell.childNodes[0];
			if (select.nodeName !== "SELECT") alert("invalid cell childnode name");
			obj.category = "Other";
			if (select.selectedIndex > 0)
				obj.category = select.options[select.selectedIndex].innerText;
			items.push(obj);
		}
		if (items.length == 0) continue;
		jsonNodes.push(new storeNode(storeName,items));
	}	
	if (jsonNodes.length == 0) return false;
	
	var myJSON = JSON.stringify(jsonNodes);
	var jsonArray = document.getElementById(jsonid); 
	jsonArray.value = myJSON;
	return true;
}
function JSONFullTable(parm,jsonid) {
	var jsonNodes	= [];
	for (var name in deleteNodes) {
			var storeArray = []; 
			for (var store in deleteNodes[name].stores) {
				storeArray.push(deleteNodes[name].stores[store]);
			}
			deleteNodes[name].stores = storeArray;
			jsonNodes.push(deleteNodes[name]);
    }
	if (jsonNodes.length == 0) return false;
	
	var myJSON = JSON.stringify(jsonNodes);
	var jsonArray = document.getElementById(jsonid); 
	jsonArray.value = myJSON;
	return true;
}
function bldJSON(jsonid) {
	var jsonNodes	= [];
	collectcheckedbox();
	for (var name in itemNodes) {
		if (itemNodes[name].DBUpdate || itemNodes[name].temporary)
			jsonNodes.push(itemNodes[name]);
    }
	if (jsonNodes.length == 0) return false;
	
	var myJSON = JSON.stringify(jsonNodes);
	var jsonArray = document.getElementById(jsonid); 
	jsonArray.value = myJSON;
	return true;
}
function updateDescr(desc) {
	var jsonNodes	= [];
	var tr = desc.parentElement.parentElement;
	if (tr.nodeName != "TR") return false;
	
	var key = tr.firstChild.innerText;
	itemNodes[key].desc = desc.value;
	jsonNodes.push(itemNodes[key]);
	updateSingleItem2(jsonNodes);
	return false;
}
function updateSingleItem(item) {
	var key = item.value;
	if (itemNodes[key] == null) return;
	var jsonNodes	= [];
	itemNodes[key].selected = item.checked;
	jsonNodes.push(itemNodes[key]);
	updateSingleItem2(jsonNodes);
}
function updateSingleItem2(jsonNodes) {
	var encoded = encodeURIComponent(JSON.stringify(jsonNodes));
	var key = "ajaxs/getUpdateSingleItem.xhtml?jsonarray=";
	var url = key+encoded+"&listname="+document.getElementById('listname').value;
	var obj = new ajaxObj(url);
	obj.key = key+jsonNodes[0],name;
	obj.ajaxFunc = function(objResp) {
		//if (obj.ajaxError) alert("posible communication error");
	}
	ajaxRequest(obj); 
}
function popUp(url) {
	genPopUp = window.open(url,"pop","toolbar=no,location=no,scrollbars=yes,directories=no,status=yes,menubar=no,resizable=yes,width=500,height=350");
	genPopUp.focus();
}
function UploadImage(parm) {
	var trTag = parm.parentNode.parentNode;
	var itemName = trTag.firstChild.innerText.replace(/^\s+|\s+$/g, '');
	var jsonNode = new itemNode(itemNodes[itemName]); 

	popUp("loadPhotos.xhtml?jsonitem="+encodeURIComponent(JSON.stringify(jsonNode)));
	return false;
}
function displayImage(parm) {
	var trTag = parm.parentNode.parentNode;
	var itemName = trTag.firstChild.innerText.replace(/^\s+|\s+$/g, '');
	var jsonNode = new itemNode(itemNodes[itemName]); 
	var ndx = window.document.baseURI.lastIndexOf("/");
	var url = window.document.baseURI;
	if (ndx != -1 ) url = url.substring(0,ndx);

	popUp(url+"/image/displayPhotos.xhtml?jsonitem="+encodeURIComponent(JSON.stringify(jsonNode)));
	return false;
}
function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
function setjsonitem() {
	var value = getParameterByName('jsonitem');
	var el = document.getElementById("jsonitem");
	if (el != null) el.value = value;
}
function getJsonSales(storeNodes,store) {
	var url = "ajaxs/ajaxGetJsonSales.xhtml?store="+store;
	var el2 = document.createElement("div"); 
	var obj = new ajaxObj(url,el2);
	obj.ajaxFunc = function(objResp) {
		objResp.ajaxDestEl.innerHTML = objResp.ajaxmsg;
		var json = objResp.ajaxDestEl.innerText;
		var dummy = JSON.parse(json, function (key, value) {
			if (key.length > 0) {
				var items = JSON.parse(value);
				salesNodes[key] = new salesNode(key,items);
			}
		});
		compareSales(storeNodes,store);
	}
	ajaxRequest(obj);
} 
function cleanSalesInfo(salesTable) {
	/*findElement("infoMessage").innerText = "";*/
	var el = findElement("salesbutton");
	if (el != null) el.style="display:none";
	salesObj = [];
	if (salesTable == null) return false;
	var len = salesTable.rows.length;
	for (var i=0;i<len;i++) {
		salesTable.deleteRow(0);
	}
	return false;
}
function showSalesInfo() {
	document.getElementById("salesTable").style.display="block";
	return false;
}
function addSalesRows(table) {
	var newRow = table.insertRow(-1);
	var cell = newRow.insertCell(-1);		
	cell.innerHTML = "<b>Sales List</b>";
	for (var i=0;i<salesObj.length;i++) {		
		newRow = table.insertRow(-1);
		cell = newRow.insertCell(-1);
		cell.innerHTML = salesObj[i].name;
    }
	var newRow = table.insertRow(-1);
	cell = newRow.insertCell(-1);		
	var cmd = 'document.getElementById("salesTable").style.display="none"';
	cell.innerHTML = "<input type='button' id='endView' style='float: right;' onclick='"+cmd+"' value='remove' />";
}
function bldSalesPanel(salesTable) {
	findElement("salesbutton").style="display:block";
	addSalesRows(salesTable);
	salesTable.style="display: none;z-index: 2;position: absolute;background-color: yellow;";
}
function compareSales(storeNodes,store) {
	var salesTable = findElement("salesTable");
	cleanSalesInfo(salesTable);
	salesTable.className="center";

	if ((typeof salesNodes[store] == 'undefined') || (salesNodes[store].items == null)) return;
	var salesItems = salesNodes[store].items;
	for (var i=0;i<salesItems.length;i++) {
		compareToStores(storeNodes,salesItems[i])
	}
	if (salesObj.length > 0) bldSalesPanel(salesTable);

	if (launchTID != null) clearInterval(launchTID);
	launchTID = null;
	setMsg(store+" has some sales that match your list","infoMessage");
}
function compareToStores(storeNodes,saleObj) {
	saleWords = saleObj.name.split(" ");
	for (var name in storeNodes) {
		var storeWords = name.split(" ");
		var ctr = 0;
		for (var i=0;i<saleWords.length;i++) {
			for (var j=0;j<storeWords.length;j++) {
				if (saleWords[i].toUpperCase() === storeWords[j].toUpperCase()) ctr++;
			}
			if (ctr == storeWords.length) {
				salesObj.push(saleObj);
			} else if (ctr > 0) partialMaches.push(saleObj);
		}
	}
}
function bldTempItem() {
	var modal = document.getElementById('myModal');
    modal.style.display = "block";
}
function itemExist(cmpName) {
	for (var name in itemNodes) {
		if (name.trim().toUpperCase() === cmpName.trim().toUpperCase()) {
			if (name.length > 0)
				return true;
		}
	}
	return false;
}
function addTempItem(parm) {
	var modal = document.getElementById('myModal');
	if (itemExist(parm.value)) {
			alert("item "+parm.value+" already exist");
			modal.style.display = "none";
			return;
	}
	var familyId = -2;
	for (var name in itemNodes) {
		familyid = itemNodes[name].familyid;
		break;
	}

	var obj = new itemNodeTemplate();
	obj.name = parm.value;
	obj.selected = true;
	obj.temporary = true;
	obj.DBUpdate  = true;
	obj.category  = "Other";
	obj.familyid  = familyid;
	for (var i=0;i<storeNames.length;i++) {
		obj.stores[storeNames[i]] = new storeNode(storeNames[i]);
	}
	itemNodes[obj.name] = obj;
	refresh(itemNodes,false);
	modal.style.display = "none";
	var jsonNodes	= [];
	jsonNodes.push(obj);
	updateSingleItem2(jsonNodes);

}
function bldItemTable() {
	var parm = document.getElementById('storelist');
	var storeArray = parm.value.split('\n');
	var modal = document.getElementById('myModal');
	var storeTbl = document.getElementById('storeTbl');
    modal.style.display = "block";
	
	removeElement("storeTbl");
	var header = storeTbl.createTHead();
	var row = header.insertRow(0);     
	var cell = row.insertCell();
	cell.innerHTML = "Item name";
	for (var i=0;i<storeArray.length;i++) {
		if (storeArray[i].trim().length == 0) continue;
		cell = row.insertCell();
		cell.innerHTML = storeArray[i].trim();
	}
	var obj = new itemNodeTemplate();
	for (var i=0;i<10;i++) {
		row = storeTbl.insertRow();
		cell = row.insertCell();
		cell.appendChild(document.createElement("input"));
		for (var j=0;j<storeArray.length;j++) {
			if (storeArray[j].trim().length == 0) continue;
			cell = row.insertCell();
			obj.name = storeArray[j].trim();
			addCheckbox(obj,cell,true);
		}
	}
	return false;
}
function exitTask(parm) {
	initStoreList();
	var modal = document.getElementById(parm);
    modal.style.display = "none";
	return false;
}
function addShoppingList() {
	tableAsJSON('storeTbl','jsonarray');
	var encoded = encodeURIComponent(JSON.stringify(document.getElementById('jsonarray').value));
	var url = "ajaxs/addItemList.xhtml?jsonarray="+encoded;
	var obj = new ajaxObj(url);
	obj.ajaxFunc = function(objResp) {
		window.open(pageName,"_parent");
	}
	ajaxRequest(obj); 
}
function showSelectedItems() {
	var selectedItems = document.getElementById("clipboard");
	selectedItems.value = "";
	collectcheckedbox();
	store=document.getElementById("storename").value; 
	for (var name in itemNodes) {
		if (store.length > 0 )  {
			if (typeof itemNodes[name].stores[store] === 'undefined')  continue;
			if (typeof itemNodes[name].stores[store].name === 'undefined')  continue;
			if (store.localeCompare(itemNodes[name].stores[store].name) != 0)  continue;
		}
		if (itemNodes[name].selected) 
			selectedItems.value += itemNodes[name].name +" "+ itemNodes[name].desc+"\n"
		
    }
	var modal = document.getElementById('myModal2');
    modal.style.display = "block";
    selectFromClipboard();
    exitTask('myModal2')
}
function bldJSONToClear(jsonid) {
	var jsonNodes	= [];
	collectcheckedbox();
	var storename = document.getElementById("storename").value;
	if (storename == null || storename.length == 0) {
		alert("to clear ; You must select a Store");
		return false;
	}
	
	if (!confirm("this request will clear all items")) return false;
	for (var name in itemNodes) {
		if (itemNodes[name].selected && itemNodes[name].stores[storename] != null) {
			itemNodes[name].selected = false;
			jsonNodes.push(itemNodes[name]);
		}
    }
	if (jsonNodes.length == 0) return false;
	
	var myJSON = JSON.stringify(jsonNodes);
	var jsonArray = document.getElementById(jsonid); 
	jsonArray.value = myJSON;
	return true;
}
function displayMyModal3() {
	var storename = document.getElementById("storename").value;
	if (storename == null || storename.length == 0) {
		alert("You must select a Store");
		return false;
	}
	var tbl = document.getElementById("myModal3List");
	tbl.innerHTML = "";
	var storeNodes = populateStore(storename);
	refresh(storeNodes,true);
	bldMultipleSelect("myModal3List",storeNodes,true);

	var modal = document.getElementById('myModal3');
    modal.style.display = "block";
	return false;
}