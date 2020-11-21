//upgrade browser for old browser
if (typeof fetch !== 'function') {
    window.open('/browser.html', '_self');
}

//functions
function htmlEncode(str) {
    return document.createElement('a').appendChild(
        document.createTextNode(str)).parentNode.innerHTML;
}

function htmlDecode(value) {
    return "";
}

function priceFormat(num) {
    num = num.toString();
    len = num.length;
    if (len == 1) {
        return "0.0" + num;
    } else if (len == 2) {
        return "0." + num;
    } else {
        return num.substr(0, len - 2) + "." + num.substr(len - 2, 2);
    }
}

function subString(s, n) {
    return s.slice(0, n).replace(/([^x00-xff])/g, "$1a").slice(0, n).replace(/([^x00-xff])a/g, "$1");
}

document.page = {
    captcha: document.getElementById("captcha"),
    currencySymbol: '￥',
    posting: false
};

if (document.page.captcha !== null) {
    document.page.captcha.style.cursor = "pointer";
    document.page.captcha.onclick = function () {
        this.src = "/captcha?" + new Date().getTime();
    };
    document.page.captcha.click();
}

/**
 * delete request
 */
function deleteRequest(path, msg, callback) {
    if (msg == null) {
        msg = "确定要删除么？";
    }
    if (typeof callback !== 'function') {
        callback = function () {
        };
    }
    if (confirm(msg)) {
        fetch(path, {
            "method": "post"
        }).then(function (response) {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(function (data) {
            if (data === null) {
                return;
            }
            if (typeof data.msg === "string" && data.msg.length > 0) {
                alert(data.msg);
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    }
}


document.querySelectorAll("form").forEach(function (form) {
    var method = form.getAttribute("method");
    if (method === null) {
        method = "get";
    }
    var ajax = form.getAttribute("ajax");
    if (ajax === null) {
        ajax = "on";
    }
    if (method.toLowerCase() !== "post" || ajax.toLowerCase() !== "on") {
        return;
    }

    form.addEventListener("submit", function (ev) {
        ev.preventDefault();
        if (document.page.posting) {
            return;
        }
        document.page.posting = false;
        var formData = new FormData(form);
        fetch(form.action, {
            method: "post",
            body: formData,
            credentials: "same-origin"
        }).then(function (response) {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(function (data) {
            if (data === null) {
                return;
            }
            if (typeof data.msg === "string" && data.msg.length > 0) {
                alert(data.msg);
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            // refresh captcha
            if (typeof data.captcha === "boolean" && data.captcha) {
                var tmp = document.querySelector("input[name=captcha]");
                if (tmp !== null) {
                    tmp.value = "";
                }
                if (document.page.captcha !== null) {
                    document.page.captcha.click();
                }
            }
            form.querySelectorAll(".inputError").forEach(function (ele) {
                ele.classList.remove("inputError");
            });
            var fe = form.querySelector("#formError");
            if (fe !== null) {
                fe.innerText = "";
                fe.style.display = "none";
            }

            if (typeof data.error == "object") {
                var i = 0;
                for (var key in data.error) {
                    if (i === 0 && fe !== null && data.error[key] !== null) {
                        fe.innerText = data.error[key];
                        fe.style.display = "block";
                    }
                    var ele = form.querySelector("input[name=" + key + "]");
                    if (typeof ele == "object") {
                        ele.classList.add("inputError");
                    }
                    i++;
                }
            }
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    });
});

