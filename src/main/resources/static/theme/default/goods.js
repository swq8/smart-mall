let mainImg = document.querySelector("#mainImg");
let smallImgs = document.querySelectorAll(".goodsImg .small img");

let floatBox = document.querySelector("#floatBox");
let mark = document.querySelector("#mark");
let bigBox = document.querySelector("#bigBox");
smallImgs.forEach(ele => {
    ele.addEventListener("mouseover", function () {
        mainImg.src = ele.src;
        bigBox.style.backgroundImage = "url('" + ele.src + "')";
        smallImgs.forEach(i => i.style.borderColor = "#fff");
        ele.style.borderColor = "#e53e41";
    });
});

bigBox.style.top = mark.offsetTop - 2 + "px";
bigBox.style.left = mark.offsetLeft + mark.offsetWidth + 6 + "px";
mark.addEventListener("mouseover", e => {
    floatBox.style.visibility = "visible";
    bigBox.style.visibility = "visible";
});

const minLeft = mark.offsetLeft;
const maxLeft = mark.offsetLeft + mark.offsetWidth - floatBox.offsetWidth;
const minTop = mark.offsetTop;
const maxTop = mark.offsetTop + mark.offsetHeight - floatBox.offsetHeight;
mark.addEventListener("mousemove", env => {
    let left = window.scrollX + env.clientX - floatBox.offsetWidth / 2;
    let top = window.scrollY + env.clientY - floatBox.offsetHeight / 2;
    if (left < minLeft) {
        left = minLeft;
    } else if (left > maxLeft) {
        left = maxLeft;
    }
    if (top < minTop) {
        top = minTop
    } else if (top > maxTop) {
        top = maxTop;
    }
    floatBox.style.left = left + "px";
    floatBox.style.top = top + "px";
    bigBox.style.backgroundPosition = "left " + (minLeft - left) * 2 + "px top " + (minTop - top) * 2 + "px";
});

mark.addEventListener("mouseout", e => {
    floatBox.style.visibility = "hidden";
    bigBox.style.visibility = "hidden";
});

//goods spec
var price = document.querySelector("#price");
var stock = document.querySelector("#stock");
var specs = document.querySelector("#specs");
if (spec.length == 0) {
    stock.innerText = stockNum;
} else {
    specs.style.display = "block";
}

spec.forEach(item => {
    let span = document.createElement("span");
    span.classList.add("tag");
    span.classList.add("tagSpec");
    span.innerText = item.name + ": ";
    specs.append(span);
    let div = document.createElement("div")
    item.list.forEach(value => {
        let a = document.createElement("a");
        let i = document.createElement("i");
        if (value.img.length > 10) {
            let img = document.createElement("img");
            img.src = value.img;
            a.append(img);
        }
        i.hint = value.hint;
        i.innerText = value.val;
        a.append(i);
        div.append(a);
    });
    specs.append(div)
});
specItems.forEach(item => {
    if (item.price > maxPrice) {
        maxPrice = item.price;
    }
});
if (maxPrice > minPrice) {
    price.innerText = priceFormat(minPrice) + " - " + priceFormat(maxPrice);
} else {
    price.innerText = priceFormat(minPrice);
}

// get selected goods specification item data
function getSpecItem() {
    let des = "";
    specs.querySelectorAll("div a.chk i").forEach(a => {
        des += a.innerText + " ";
    });
    des = des.trim();
    for (var i = 0; i < specItems.length; i++) {
        var item = specItems[i];
        if (des === item.des) {
            return item;
        }
    }
    return null;
}

// update price and stock by specification checked
function updatePrice() {
    var item = getSpecItem();
    if (item == null) {
        return;
    }
    price.innerText = priceFormat(item.price);
    stock.innerText = item.stock;
}

document.querySelectorAll("#specs a").forEach(a => {
    a.addEventListener("click", env => {
        a.parentElement.querySelectorAll("a").forEach(aa => aa.classList.remove("chk"));
        a.classList.add("chk");
        updatePrice();
    });
});

document.querySelector("#addToCart").addEventListener("click", env => {
    let gid = parseFloat(document.querySelector("#goodsId").value);
    let specId = 0;
    let num = parseInt(document.querySelector("#num").value);

    if (spec.length > 0) {
        let item = getSpecItem();
        if (item == null) {
            alert("请选择规格");
            return;
        }
        specId = item.id;
        if (num > item.stock) {
            alert("库存不足");
            return;
        }
    }
    window.location = "/cart/add?gid=" + gid + "&specId=" + specId + "&num=" + num;

});