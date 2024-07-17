var $ = function (selectors) {
    return document.querySelector(selectors);
}

//functions
function htmlEncode(value) {
    return "";
}

function htmlDecode(value) {
    return "";
}

function priceFormat(num) {
    return num.toFixed(2);
}

function subString(s, n) {
    return s.slice(0, n).replace(/([^x00-xff])/g, "$1a").slice(0, n).replace(/([^x00-xff])a/g, "$1");
}

function hideToast() {
    document.hideToastId = undefined
    let toastBox = document.querySelector("#toast-box-parent")
    if (toastBox !== null) {
        document.body.removeChild(toastBox)
    }
}

/**
 * @param msg     要显示的消息
 * @param timeout 隐藏延时(毫秒) 默认三秒, 0为不自动隐藏
 */
function showToast(msg, timeout = 3500) {
    clearInterval(document.hideToastId)
    hideToast()
    let toastBoxParent = document.createElement("div")
    toastBoxParent.setAttribute("id", "toast-box-parent")
    let toastBox = document.createElement("div")
    toastBox.setAttribute("id", "toast-box")
    toastBox.innerText = msg
    toastBoxParent.append(toastBox)
    document.body.append(toastBoxParent)
    if (timeout > 0) {
        document.hideToastId = setTimeout(evt => {
            hideToast()
        }, timeout)
    }
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
    if (msg != null && !confirm(msg)) {
        return;
    }
    if (typeof callback !== 'function') {
        callback = function () {
        };
    }
}

/*  header cart begin */
const headerCart = $("#headerCart");
const headerCartNum = $("#headerCartNum");
const headerCartBtn = $("#headerCartBtn");
const headerCartList = $("#headerCartList");
const headerCartItems = $("#headerCartItems");
const headerCartFooter = $("#headerCartFooter");

function renderHeaderCart(param) {
    if (typeof param === "undefined") {
        param = "";
    }
    fetch("/cart/json" + param, {
        method: "get",
        credentials: "same-origin"
    }).then(response => {
        if (response.status !== 200) {
            alert("/cart/json 服务器返回异常,请重试!\r\nresponse status code:" + response.status);
            return null;
        }
        return response.json()
    }).then(cartItems => {

        if (param.length > 0) {
            headerCart.removeEventListener("mouseenter", headerCartMouseEnter);
            headerCart.removeEventListener("mouseleave", headerCartMouseLeave);
        }

        headerCartItems.innerHTML = "";
        let num = 0;
        cartItems.forEach(item => {
            let div = document.createElement("div");
            div.classList.add("item");
            let a = document.createElement("a");
            a.classList.add("img");
            a.href = "/goods/" + item.goodsId + ".html";
            let img = document.createElement("img");
            img.src = item.goodsImg;
            a.append(img);
            div.append(a);
            let div1 = document.createElement("div");

            div1.classList.add("name");
            a = document.createElement("a");
            a.href = "/goods/" + item.goodsId + ".html";
            a.innerText = item.goodsName;
            a.title = item.goodsName;
            div1.append(a);
            div.append(div1);
            let footer = document.createElement("div");
            footer.classList.add("footer");
            let span = document.createElement("span");
            span.classList.add("spec");
            span.innerText = item.specDes;
            footer.append(span);
            span = document.createElement("span");
            span.classList.add("price");
            span.innerText = priceFormat(item.goodsPrice);
            footer.append(span);
            span = document.createElement("span");
            span.classList.add("num");
            span.innerText = item.num;
            footer.append(span);
            div.append(footer);
            a = document.createElement("a");
            a.href = "javascript:renderHeaderCart('?m=del&goodsId=" + item.goodsId + "&specId=" + item.specId + "');";
            a.innerText = "删除";
            footer.append(a);
            div.append(footer);
            headerCartItems.append(div);
            num += item.num;
        });
        headerCartNum.innerHTML = num;
        if (cartItems.length === 0) {
            headerCartFooter.style.display = "none";
        } else {
            headerCartFooter.style.display = "";
        }
        if (param.length > 0) {
            setTimeout(ev => {
                headerCart.addEventListener("mouseenter", headerCartMouseEnter);
                headerCart.addEventListener("mouseleave", headerCartMouseLeave);
            }, 50);
        }
    });
}


function headerCartMouseEnter() {
    headerCartBtn.style.borderBottomColor = "#fff";
    headerCartList.style.display = "block";
    renderHeaderCart();
}


function headerCartMouseLeave() {
    headerCartList.style.display = "none";
    headerCartBtn.style.borderBottomColor = "";
}

headerCart.addEventListener("mouseenter", headerCartMouseEnter);
headerCart.addEventListener("mouseleave", headerCartMouseLeave);

if (window.location.pathname !== "/cart") {
    renderHeaderCart();
}

/* header cart end */

/* header menu begin */
const headerMenu = $("#headerMenu");
if (window.location.pathname !== "/") {
    headerMenu.addEventListener("mouseenter", evt => {
        headerMenuItems.style.display = "block";
    });
    headerMenu.addEventListener("mouseleave", evt => {
        headerMenuItems.style.display = "none";
    });
}
/* header menu end */

document.querySelectorAll("form").forEach(form => {
    // 有vue绑定的跳过
    if (form.getAttribute("@submit.prevent") !== null || form.getAttribute("v-on:submit.prevent") !== null) {
        return
    }

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

    form.addEventListener("submit", evt => {
        evt.preventDefault();
        if (document.page.posting) {
            return;
        }
        document.page.posting = false;
        var formData = new FormData(form);
        fetch(form.action, {
            method: "post",
            body: formData,
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(data => {
            if (data === null) {
                return;
            }
            if (data.msg && data.msg.length > 0) {
                showToast(data.msg);
            } else {
                for (let key in data.error) {
                    showToast(data.error[key])
                    break
                }
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            // refresh captcha
            if (typeof data.captcha === "boolean" && data.captcha) {
                var tmp = $("input[name=captcha]");
                if (tmp !== null) {
                    tmp.value = "";
                }
                if (document.page.captcha !== null) {
                    document.page.captcha.click();
                }
            }
            form.querySelectorAll(".inputError").forEach(el => {
                el.classList.remove("inputError");
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

class Region {
    static data = null;

    // constructor
    constructor(el1, el2, el3, code) {
        this.el1 = el1;
        this.el2 = el2;
        this.el3 = el3;
        if (typeof code === "undefined" || code < 110000) {
            code = 0;
        }
        this.code = code;
        if (Region.data === null) {
            this.init();
        } else {
            this.initEvt();
        }
    }

    init() {
        fetch("/region", {
            method: "get",
            credentials: "same-origin"
        }).then(function (response) {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(json => {
            Region.data = json;
            this.initEvt();
        });
    }

    initEvt() {
        this.el1.innerHTML = `<option value="0">请选择</option>`;
        this.el2.innerHTML = `<option value="0">请选择</option>`;
        this.el3.innerHTML = `<option value="0">请选择</option>`;
        Region.data.forEach(province => {
            let option = document.createElement("option");
            option.value = province.code;
            option.innerText = province.name;
            if (Math.floor(this.code / 10000) * 10000 == province.code) {
                option.selected = true;
                province.children.forEach(city => {
                    let option2 = document.createElement("option");
                    option2.value = city.code;
                    option2.innerText = city.name;
                    if (Math.floor(this.code / 100) * 100 == city.code) {
                        option2.selected = true;
                        city.children.forEach(area => {
                            let option3 = document.createElement("option");
                            option3.value = area.code;
                            option3.innerText = area.name;
                            if (this.code == area.code) {
                                option3.selected = true;
                            }
                            this.el3.append(option3);
                        });
                    }
                    this.el2.append(option2);
                });
            }
            this.el1.append(option);
        });
        this.el1.addEventListener("change", evt => {
            this.el2.innerHTML = `<option value="0">请选择</option>`;
            this.el3.innerHTML = `<option value="0">请选择</option>`;
            let code = parseInt(this.el1.value);
            Region.data.forEach(province => {
                if (province.code == code) {
                    province.children.forEach(city => {
                        let option = document.createElement("option");
                        option.value = city.code;
                        option.innerText = city.name;
                        this.el2.append(option);
                    });
                    return;
                }
            });
        });

        this.el2.addEventListener("change", evt => {
            this.el3.innerHTML = `<option value="0">请选择</option>`;
            let code = parseInt(this.el2.value);
            let pCode = Math.floor(code / 10000) * 10000;
            Region.data.forEach(province => {
                if (province.code == pCode) {
                    province.children.forEach(city => {
                        if (city.code == code) {
                            city.children.forEach(area => {
                                let option = document.createElement("option");
                                option.value = area.code;
                                option.innerText = area.name;
                                this.el3.append(option);
                            });
                            return;
                        }

                    });
                }
            });
        });


    }
}
