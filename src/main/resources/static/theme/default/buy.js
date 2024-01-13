const model1 = $("#model1");
$("#addAddr").addEventListener("click", evt => {
    model1.style.display = "block";
    new Region($("#province"), $("#city"), $("#area"));
});
$("button#cancel").addEventListener("click", evt => {
    model1.style.display = "none";
});

$("select[name=addrId]").addEventListener("change", ev => {
    location.href = "?addrId=" + ev.target.value;
});
