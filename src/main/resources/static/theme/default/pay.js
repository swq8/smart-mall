// order no
const orderNo = document.querySelector("#orderNo").value;

var counterSpan = document.querySelector("#counter");
var qrImg = document.querySelector("#qr");
// 自动刷新倒计时
var counter = 60;

// 刷新付款二维码
function flushQr() {
    qrImg.src = "/user/order/payQr?orderNo=" + orderNo + "&" + new Date().getTime();
    counter = 60;
}

setInterval(function () {
    if (--counter < 1) {
        counter = 60;
        flushQr();
    }
    counterSpan.innerHTML = counter;
}, 1000);
document.querySelector("#flush").addEventListener("click", evt => {
    flushQr();
});
// 每三秒检测下支付状态
setInterval(function () {
    fetch("/user/order/payCheck?orderNo=" + orderNo, {
        credentials: "same-origin"
    }).then(response => {
        if (response.status !== 200) {
            alert("/cart/json 服务器返回异常,请重试!\r\nresponse status code:" + response.status);
            return null;
        }
        return response.json()
    }).then(data => {
        if (typeof data.url === "string" && data.url.length > 0) {
            document.location = data.url;
        }
    });
}, 2900);