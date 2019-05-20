filterTaskSelection("all")
function filterTaskSelection(c) {
    var lang = document.getElementsByClassName("activeLang")[0].id;
    var x, i;
    x = document.getElementsByClassName("taskFilter");
    if (c == "all") c = "";
    for (i = 0; i < x.length; i++) {
        w3RemoveClass(x[i], "show");
        if (x[i].className.indexOf(c) > -1) {
            if (lang === "allLang" || x[i].className.indexOf(lang) > -1) {
                w3AddClass(x[i], "show");
            }
        }
    }
}

filterLangSelection("all")
function filterLangSelection(c) {
    var task = document.getElementsByClassName("activeTask")[0].id;
    var x, i;
    x = document.getElementsByClassName("taskFilter");
    if (c == "all") c = "";
    for (i = 0; i < x.length; i++) {
        w3RemoveClass(x[i], "show");
        if (x[i].className.indexOf(c) > -1) {
            if (task === "allTask" || x[i].className.indexOf(task) > -1) {
                w3AddClass(x[i], "show");
            }
        }
    }
}

function w3AddClass(element, name) {
    var i, arr1, arr2;
    arr1 = element.className.split(" ");
    arr2 = name.split(" ");
    for (i = 0; i < arr2.length; i++) {
        if (arr1.indexOf(arr2[i]) == -1) {element.className += " " + arr2[i];}
    }
}

function w3RemoveClass(element, name) {
    var i, arr1, arr2;
    arr1 = element.className.split(" ");
    arr2 = name.split(" ");
    for (i = 0; i < arr2.length; i++) {
        while (arr1.indexOf(arr2[i]) > -1) {
            arr1.splice(arr1.indexOf(arr2[i]), 1);
        }
    }
    element.className = arr1.join(" ");
}

// Add active class to the current task filter button (highlight it)
var taskBtnContainer = document.getElementById("taskBtnContainer");
var taskBtns = taskBtnContainer.getElementsByClassName("btn");
for (var i = 0; i < taskBtns.length; i++) {
    taskBtns[i].addEventListener("click", function(){
        var current = document.getElementsByClassName("activeTask");
        current[0].className = current[0].className.replace(" activeTask", "");
        this.className += " activeTask";
    });
}

// Add active class to the current language filter button (highlight it)
var langBtnContainer = document.getElementById("langBtnContainer");
var langBtns = langBtnContainer.getElementsByClassName("btn");
for (var i = 0; i < langBtns.length; i++) {
    langBtns[i].addEventListener("click", function(){
        var current = document.getElementsByClassName("activeLang");
        current[0].className = current[0].className.replace(" activeLang", "");
        this.className += " activeLang";
    });
}
