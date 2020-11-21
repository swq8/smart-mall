const img = document.getElementById("img");
const imgs = document.getElementById("imgs");
const spinner = document.getElementById("spinner");

function getInputValue(name) {
    let input = document.querySelector("input[name=" + name +"]");
    if (input === null) {
        return "";
    } else {
        return input.value;
    }
}
function moveLeft(ele) {
    let div1 = ele.parentElement.parentElement;
    let div2 = div1.previousSibling;
    if (div2.nodeName.toLocaleLowerCase() !== "div") {
        return;
    }
    imgs.insertBefore(div1, div2);
}

function moveRight(ele) {
    let div1 = ele.parentElement.parentElement;
    let div2 = div1.nextElementSibling;
    if (div2.nodeName.toLocaleLowerCase() !== "div") {
        return;
    }
    imgs.insertBefore(div2, div1);
}

function addImg(uri) {
    let div = document.createElement("div");
    let input = document.createElement("input");
    input.type = "hidden";
    input.name = "img";
    input.value = uri;
    let imgEle = document.createElement("img");
    let p = document.createElement("p");
    p.classList.add("text-center");
    let leftA = document.createElement("a");
    leftA.href = "javascript:;";
    leftA.setAttribute("onclick", "moveLeft(this)");
    let leftI = document.createElement("i");
    leftI.classList.add("fa");
    leftI.classList.add("fa-arrow-left");
    leftI.classList.add("fa-lg");
    leftA.appendChild(leftI);
    p.appendChild(leftA);
    let removeA = document.createElement("a");
    removeA.href = "javascript:;";
    removeA.setAttribute("onclick", "this.parentElement.parentElement.remove()");
    removeA.classList.add("remove");
    let removeI = document.createElement("i");
    removeI.classList.add("fa");
    removeI.classList.add("fa-remove");
    removeI.classList.add("fa-lg");
    removeA.appendChild(removeI);
    p.appendChild(removeA);
    let rightA = document.createElement("a");
    rightA.href = "javascript:;";
    rightA.setAttribute("onclick", "moveRight(this)");
    let rightI = document.createElement("i");
    rightI.classList.add("fa");
    rightI.classList.add("fa-arrow-right");
    rightI.classList.add("fa-lg");
    rightA.appendChild(rightI);
    p.appendChild(rightA);
    imgEle.src = uri;
    div.appendChild(input);
    div.appendChild(imgEle);

    div.appendChild(p);
    imgs.appendChild(div);
}

tinymce.init({
    convert_urls: false,
    language: 'zh_CN',
    selector: '#des',
    height: '20rem',
    plugins: "autolink code fullscreen help image link media preview table",
    toolbar: 'undo redo | formatselect | ' +
        'bold italic backcolor | alignleft aligncenter ' +
        'alignright alignjustify | bullist numlist outdent indent | ' +
        'removeformat | image | fullscreen | code | preview',

    /* we override default upload handler to simulate successful upload */
    images_upload_handler: function (blobInfo, success, failure) {
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), blobInfo.filename());
        fetch('/admin/upload', {
            method: "POST",
            body: formData,
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                failure("未知错误，请联系统管理员, status:" + response.status);
                return;
            }
            response.json().then(json => {
                success(json.url);
            });
        }).catch(error => {
            failure("网络错误");
        });
    },
    setup: function (editor) {
        editor.on('change', function () {
            tinymce.triggerSave();
        });
    }
});
img.addEventListener("change", ev => {
    if (img.files.length === 0) {
        return;
    }
    let file = img.files[0];
    if (file.size > 0x100000) {
        alert("文件尺寸请勿超过1MB");
        return;
    }
    if (file.type.substring(0, 5) !== "image") {
        alert("请选择图片文件");
        return;
    }
    if (document.querySelectorAll("input[name=img]").length >= 10) {
        alert("请勿超过10张");
        img.value = "";
        return;
    }
    let data = new FormData();
    data.append('file', file);
    spinner.classList.remove("invisible");
    fetch('/admin/upload', {
        method: "POST",
        body: data,
        credentials: "same-origin"
    }).then(response => {
        spinner.classList.add("invisible");
        if (response.status !== 200) {
            alert("未知错误，请联系统管理员, status:" + response.status);
            return;
        }
        response.json().then(json => {
            img.value = "";
            addImg(json.url);
        });
    }).catch(error => {
        spinner.classList.add("invisible");
        alert("网络错误");
    });
});

//show images
if (imgsStr.length > 4) {
    imgsStr.split(",").forEach(item => {
        addImg(item)
    });
}

//init goods specification element
let specEle = document.querySelector('#specName');
let specValueEle = document.querySelector('#specValue');

let SpecObj = function () {
    this.price = "";
    this.oldStock = -1;
    this.stock = "";
    this.weight = "";
    this.spec = [];
    this.items = [];
    this.addSpec = function () {
        let id = parseInt(specEle.value);
        let valId = parseInt(specValueEle.selectedIndex);
        if (id <= 0 || valId <= 0) {
            return;
        }
        let name = this.getSpecNameById(id);
        let val = this.getSpecVal(id, valId - 1);

        // spec name exits
        let nameExits = false;
        let valueExits = false;

        this.spec.forEach(item => {
            if (item.name === name) {
                nameExits = true;
                item.list.forEach(item => {
                    if (item.val == val.val) {
                        valueExits = true;
                        return;
                    }
                });
                if (!valueExits) {
                    item.list.push(val);
                }
            }
        });
        if (valueExits) {
            return;
        }
        if (!nameExits) {
            this.spec.push({
                'name': name,
                'list': [val]
            });
        }
        this.generateItems();
        this.render();

    }
    this.delItem = function (index) {
        for (let i = 0; i < specObj.items.length; i++) {
            if (specObj.items[i].index === index) {
                specObj.items.splice(i, 1);
                break;
            }
        }
        let tr = document.querySelector("#specTable #tr_" + index);
        tr.remove();
    }
    this.delSpec = function (index) {
        if (index >= this.spec.length) {
            return;
        }
        if (!confirm("确定要删除规格 '" + this.spec[index].name + "' 么？")) {
            return;
        }
        this.spec.splice(index, 1);
        this.generateItems();
        this.render();
    }
    //generate items data
    this.generateItems = function () {
        let tmpItems = [];
        if (this.spec.length == 0) {
            this.items = tmpItems;
            return;
        }
        let itemIndex = new Array();
        for (i = 0; i < this.spec.length; i++) {
            itemIndex[i] = 0;
        }

        while (true) {
            if (itemIndex[0] >= this.spec[0].list.length) {
                break;
            }
            let item = {
                index: "",
                specId: 0,
                price: getInputValue("price"),
                oldStock: -1,
                stock: getInputValue("stock"),
                weight: getInputValue("weight")
            };
            // goods spec index and desc
            for (i = 0; ;) {
                item.index += itemIndex[i];
                i++;
                if (i < itemIndex.length) {
                    item.index += "_";
                } else {
                    break;
                }
            }
            tmpItems.push(item);
            itemIndex[itemIndex.length - 1]++;
            for (i = itemIndex.length - 1; i >= 1; i--) {
                if (itemIndex[i] >= this.spec[i].list.length) {
                    itemIndex[i] = 0;
                    itemIndex[i - 1]++;
                } else {
                    break;
                }
            }
            this.items = tmpItems;
        }
    }
    this.getDataByIndex = function (index) {
        return null;
    }
    this.getSpecNameById = function (id) {
        let name = null;
        goodsSpec.forEach(spec => {
            if (spec.id === id) {
                name = spec.name;
                return;
            }
        });
        return name;
    }
    this.getSpecVal = function (specId, valIndex) {
        let val = null;
        goodsSpec.forEach(spec => {
            if (spec.id == specId) {
                val = spec.list[valIndex];
                return;
            }
        });
        return val;
    }

    // render spec table
    this.render = function () {
        document.querySelector("input[name=spec]").value = JSON.stringify(this.spec);
        let specTable = document.querySelector("#specTable");
        //render column
        let tr = specTable.querySelector("thead tr");
        tr.querySelectorAll("th").forEach(item => {
            if (item.querySelectorAll("a").length > 0) {
                item.remove();
            }
        });
        for (i = this.spec.length - 1; i >= 0; i--) {
            let item = this.spec[i];
            let th = document.createElement("th");
            let a = document.createElement("a");
            a.href = "javascript:specObj.delSpec(" + i + ");";
            a.innerText = item.name;
            th.appendChild(a);
            tr.prepend(th);
        }
        let tbody = specTable.querySelector("tbody");
        tbody.innerHTML = "";
        if (this.spec.length == 0) {
            let tr = document.createElement("tr");
            let td = document.createElement("td");
            let input = document.createElement("input");
            input.type = "number";
            input.name = "price";
            input.value = this.price;
            input.min = 0;
            input.step = 0.01;
            input.required = true;
            td.appendChild(input);

            input = document.createElement("input");
            input.type = "hidden";
            input.name = "oldStock";
            input.value = this.oldStock;
            td.appendChild(input);

            tr.appendChild(td);

            td = document.createElement("td");
            input = document.createElement("input");
            input.type = "number";
            input.name = "stock";
            input.value = this.stock;
            input.min = 0;
            input.step = 1;
            input.required = true;
            td.appendChild(input);
            tr.appendChild(td);

            td = document.createElement("td");
            input = document.createElement("input");
            input.type = "number";
            input.name = "weight";
            input.value = this.weight;
            input.min = 0;
            input.step = 1;
            input.required = true;
            td.appendChild(input);
            tr.appendChild(td);
            tbody.appendChild(tr);
        }

        this.items.forEach(item => {
            let tr = document.createElement("tr");
            tr.id = "tr_" + item.index;
            let indexs = item.index.split("_");
            for (i = 0; i < indexs.length; i++) {
                let specValue = this.spec[i].list[parseInt(indexs[i])];
                let td = document.createElement("td");
                let html = "";
                if (specValue.img.length > 0) {
                    html += "<img class='spec' src='" + specValue.img + "'>";
                }
                html += htmlEncode(specValue.val);
                td.innerHTML = html;
                tr.appendChild(td);
            }
            let td = document.createElement("td");
            let input = document.createElement("input");
            input.type = "hidden";
            input.name = "specId";
            input.value = item.specId;
            td.appendChild(input);

            input = document.createElement("input");
            input.type = "hidden";
            input.name = "oldStock";
            input.value = item.oldStock;
            td.appendChild(input);

            input = document.createElement("input");
            input.type = "number";
            input.name = "price";
            input.value = item.price;
            input.min = 0;
            input.step = 0.01;
            input.required = true;
            td.appendChild(input);
            tr.appendChild(td);

            td = document.createElement("td");
            input = document.createElement("input");
            input.type = "number";
            input.name = "stock";
            input.value = item.stock;
            input.min = 0;
            input.step = 1;
            input.required = true;
            td.appendChild(input);
            tr.appendChild(td);

            td = document.createElement("td");
            input = document.createElement("input");
            input.type = "number";
            input.name = "weight";
            input.value = item.weight;
            input.min = 0;
            input.step = 1;
            input.required = true;
            td.appendChild(input);
            tr.appendChild(td);
            tbody.appendChild(tr);
        });
    }
}

let specObj = new SpecObj();

//add spec value for goods
specValueEle.addEventListener('change', evt => {
    specObj.addSpec();
});

// init goods spec list
goodsSpec.forEach(spec => {
    let ele = document.createElement('option');
    let text = spec.name + " [" + spec.note + "]";
    ele.innerText = text;
    ele.value = spec.id;
    specEle.append(ele);
});

specEle.addEventListener("change", evt => {
    specValueEle.innerHTML = "<option value=''>请选择</option>";
    let specId = evt.target.value;
    if (specId <= 0) {
        return;
    }
    goodsSpec.forEach(spec => {
        if (specId != spec.id) {
            return;
        }
        spec.list.forEach(item => {
            let ele = document.createElement("option");
            ele.innerText = item.val;
            specValueEle.append(ele);
        });
    });
});