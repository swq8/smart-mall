var cartList = document.querySelector("#cartList");
var cartFooter = cartList.querySelector(".footer");
const footHtml = `

`;

function renderCartList(param) {
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
        cartList.querySelectorAll("tr.item").forEach(tr => tr.remove());
        let isSelectAll = true;
        let sumNum = 0;
        let sumPrice = 0;
        let sumWeight = 0;
        cartItems.forEach(item => {
            let tr = document.createElement("tr");
            tr.classList.add('item');
            tr.setAttribute("data-goodsId", item.goodsId);
            tr.setAttribute("data-specId", item.specId);
            tr.setAttribute("data-num", item.num);
            let td = document.createElement("td");
            let input = document.createElement("input");
            input.type = "checkbox";
            if (item.selected) {
                input.checked = true;
                input.addEventListener("click", ev => selected(ev, false));
            } else {
                isSelectAll = false;
                input.addEventListener("click", ev => selected(ev, true));
            }
            td.append(input);
            tr.append(td);
            td = document.createElement("td");
            let img = document.createElement("img");
            img.src = item.goodsImg;
            td.append(img);
            tr.append(td);
            td = document.createElement("td");
            let a = document.createElement("a");
            a.innerText = item.goodsName;
            a.href = "/goods/" + item.goodsId + ".html";
            td.append(a);
            if (item.specDes != null && item.specDes.length > 0) {
                let span = document.createElement("span");
                span.classList.add("specDes");
                span.innerText = item.specDes;
                td.append(span);
            }

            tr.append(td);
            td = document.createElement("td");
            td.innerText = item.goodsWeight + "g";
            tr.append(td);
            td = document.createElement("td");
            td.innerText = priceFormat(item.goodsPrice);
            tr.append(td);
            td = document.createElement("td");
            let div = document.createElement("div");
            div.classList.add("num");
            a = document.createElement("a");
            a.classList.add("sub", "btn");
            a.addEventListener("click", ev => sub(ev));
            div.append(a);
            input = document.createElement("input");
            input.type = "number";
            input.min = 1;
            input.value = item.num;
            sumNum += item.num;
            div.append(input);
            a = document.createElement("a");
            a.classList.add("add", "btn");
            a.addEventListener("click", ev => add(ev));
            div.append(a);
            td.append(div);
            tr.append(td);
            td = document.createElement("td");
            td.classList.add("price");
            td.innerText = priceFormat(item.goodsPrice * item.num);
            if (item.selected) {
                sumPrice += item.goodsPrice * item.num;
                sumWeight += item.goodsWeight * item.num;
            }
            tr.append(td);
            td = document.createElement("td");
            a = document.createElement("a");
            a.href = "javascript:renderCartList('?m=del&goodsId=" + item.goodsId + "&specId=" + item.specId + "');";
            a.innerText = "删除";
            td.append(a);
            tr.append(td);
            cartList.append(tr);
        });
        let tr = document.createElement("tr");
        tr.classList.add('footer', 'item', 'text-right');
        let td = document.createElement("td");
        td.colSpan = 8;
        td.innerHTML = `
商品总重量:&nbsp;&nbsp;sumWeightg<br>
金额合计(不含运费):&nbsp;&nbsp;<span class='price'>sumPrice</span>
        `.replace("sumWeight", sumWeight).replace("sumPrice", priceFormat(sumPrice));
        tr.append(td);
        cartList.append(tr);
        if (sumNum > 0) {
            document.querySelector(".cartFooter").style.display = "block";
        } else {
            document.querySelector(".cartFooter").style.display = "none";
        }
        if (isSelectAll) {
            $("input#selectAll").checked = true;
            $("input#selectAll").addEventListener("click", ev => selectedAll(ev, false));
        } else {
            $("input#selectAll").checked = false;
            $("input#selectAll").addEventListener("click", ev => selectedAll(ev, true));
        }
    });
}

renderCartList();

function add(ev) {
    let tr = ev.target.parentElement.parentElement.parentElement;
    let input = ev.target.parentElement.querySelector("input");
    let goodsId = tr.getAttribute("data-goodsId");
    let specId = tr.getAttribute("data-specId");
    let n = input.value - tr.getAttribute("data-num") + 1;
    renderCartList("?m=add&goodsId=" + goodsId + "&specId=" + specId + "&num=" + n);
}
//更改单条记录选中状态
function selected(ev, selected) {
    let tr = ev.target.parentElement.parentElement;
    let goodsId = tr.getAttribute("data-goodsId");
    let specId = tr.getAttribute("data-specId");
    selected = selected ? 1 : 0;
    renderCartList("?m=selected&goodsId=" + goodsId + "&specId=" + specId + "&selected=" + selected);
}
//更改全部记录选中状态
function selectedAll(ev, selected) {
    let tr = ev.target.parentElement.parentElement.parentElement;
    let goodsId = tr.getAttribute("data-goodsId");
    let specId = tr.getAttribute("data-specId");
    selected = selected ? 1 : 0;
    renderCartList("?m=selectedAll&goodsId=" + goodsId + "&specId=" + specId + "&selected=" + selected);
}
function sub(ev) {
    let tr = ev.target.parentElement.parentElement.parentElement;
    let input = ev.target.parentElement.querySelector("input");
    let goodsId = tr.getAttribute("data-goodsId");
    let specId = tr.getAttribute("data-specId");
    let n = input.value - tr.getAttribute("data-num") - 1;
    renderCartList("?m=sub&goodsId=" + goodsId + "&specId=" + specId + "&num=" + Math.abs(n));
}

function clearCart() {
    if (confirm("确定要清空购物车么?")) {
        renderCartList("?m=clear");
    }
}
