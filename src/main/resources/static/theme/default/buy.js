const model1 = $("#model1");
$("#addAddr").addEventListener("click", evt => {
    model1.style.display = "block";
    new Region($("#province"), $("#city"), $("#area"));
});
$("button#cancel").addEventListener("click", evt => {
    model1.style.display = "none";
});

document.querySelectorAll("a.edit").forEach(el => {
    let addrId = el.parentElement.getAttribute("addr-id");
    el.addEventListener("click", evt => {
        fetch("/user/address/json?id=" + addrId, {
            method: "get",
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(json => {
            if (typeof json.err !== 'undefined') {
                alert(json.err);
                return;
            }
            $("input[name=id]").value = json.id;
            $("input[name=name]").value = json.name;
            $("input[name=phone]").value = json.phone;
            $("input[name=address]").value = json.address;
            $("input[name=dft]").checked = json.dft > 0;
            new Region($("#province"), $("#city"), $("#area"), json.region);
            model1.style.display = "block";
        });
    });
});
document.querySelectorAll("a.delete").forEach(el => {
    let addrId = el.parentElement.getAttribute("addr-id");
    el.addEventListener("click", evt => {
        if (!confirm("确定要删除该地址么？")) {
            return;
        }
        fetch("/user/address/delete?id=" + addrId, {
            method: "get",
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            location.href = "/user/address";
        });
    });
});
