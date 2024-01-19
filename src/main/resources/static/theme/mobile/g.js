// Menu
const Menu = {
    data() {
        return {
            bgCls: 'hidden',
            menuCls: 'hidden',
            items: [
                {cls: 'fa fa-home', name: '商城首页', href: '/'},
                {cls: 'fa fa-bars', name: '商品分类', href: '/category'},
                {cls: 'fa fa-bell', name: '商城快讯', href: '/article/list?cid=1'},
                {cls: 'fa fa-shopping-cart', name: '购物车', href: '/cart'},
                {cls: 'fa fa-user-o', name: '用户中心', href: '/user/central'}
            ],
        }
    },
    methods: {
        show() {
            this.bgCls = 'menu-bg-show'
            this.menuCls = 'menu-show'
        },
        hidden() {
            this.bgCls = 'hidden'
            this.menuCls = 'hidden'
        },
    },
}
Vue.createApp(Menu).mount('#menu')
// 页脚渲染
const Footer = {
    setup() {
        let items = [
            {text: "主 页", img: jsPath + "/theme/" + themeName + "/footer/home.svg", href: "/", cls: ""},
            {text: "分 类", img: jsPath + "/theme/" + themeName + "/footer/category.svg", href: "/category", cls: ""},
            {text: "购物车", img: jsPath + "/theme/" + themeName + "/footer/cart.svg", href: "/cart", cls: ""},
            {text: "我 的", img: jsPath + "/theme/" + themeName + "/footer/user.svg", href: "/user/central", cls: ""}
        ]
        let pathname = location.pathname
        let index = -1
        if (pathname === "/") {
            index = 0
        } else if (pathname === "/category") {
            index = 1
        } else if (pathname.startsWith("/cart")) {
            index = 2
        } else if (pathname.startsWith("/user")) {
            index = 3
        }
        if (index >= 0) {
            let img = items[index].img
            items[index].img = img.substring(0, img.length - 4) + "1.svg"
            items[index].cls = 'red'

        }
        return {items: items}
    }
}

Vue.createApp(Footer).mount('#footer')

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
    if (num < 10) {
        return "0.0" + num;
    } else if (num < 100) {
        return "0." + num;
    }
    let rem = num % 100;
    let i = Math.floor(num / 100);
    if (rem < 10) {
        return i + ".0" + rem;
    } else {
        return i + "." + rem;
    }
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
            if (typeof data.msg === "string" && data.msg.length > 0) {
                showToast(data.msg);
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
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    });
});

