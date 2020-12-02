var rules = $("#rules");
$("input[name=firstWeight]").value = priceRule.firstWeight;
$("input[name=firstPrice]").value = priceFormat(priceRule.firstPrice);

$("input[name=additionalWeight]").value = priceRule.additionalWeight;
$("input[name=additionalPrice]").value = priceFormat(priceRule.additionalPrice);
if (priceRule.otherDefault) {
    $("input[name=otherDefault]").checked = true;
}


$("#addBtn").addEventListener("click", ev => {
    rules.append(ele(ruleHtml));
});

function addArea(ele) {
    let hidden = ele.querySelector("input[type=hidden]");
    let option = ele.querySelector("option:checked");
    let textarea = ele.querySelector("textarea");
    let code = option.value;
    let name = option.innerHTML;
    delArea(code);
    if (hidden.value.length > 2) {
        hidden.value += ",";
    }
    hidden.value += code;
    if (textarea.textLength > 1) {
        textarea.append(",");
    }
    textarea.append(name);
}

function delArea(code) {
    document.querySelectorAll("div.rule").forEach(div => {
        let textarea = div.querySelector("textarea");
        let hidden = div.querySelector("input[type=hidden]");
        if (hidden.value.length < 6) {
            return;
        }
        let codes = hidden.value.split(",");
        hidden.value = "";

        textarea.textContent = "";
        for (var i = 0; i < codes.length; i++) {
            let c = codes[i];
            if (c === code) {
                continue;
            }
            if (hidden.value.length > 1) {
                hidden.value += ",";
                textarea.append(",");
            }
            hidden.value += c;
            textarea.append(div.querySelector("option[value='" + c + "']").text);
        }
    });
}

priceRule.provincePrices.forEach(rule => {
    var e = ele(ruleHtml);
    e.querySelector("input[name=provinces]").value = rule.provinces.join();
    e.querySelector("input[name=price1]").value = priceFormat(rule.firstPrice);
    e.querySelector("input[name=price2]").value = priceFormat(rule.additionalPrice);
    rules.append(e);
});
delArea(0);