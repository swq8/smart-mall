// vue compent, goods property
const GoodsProperty = {
    props: {
        // goods id
        goodsId: {
            type: Number,
            required: true
        },
        price: {
            type: Number,
            required: true
        },
        // stock num
        stock: {
            type: Number,
            required: true
        },
        // spec names
        spec: {
            type: Array,
            required: true,
            default: []
        },
        // spec items
        specItems: {
            type: Array,
            required: true
        }
    },
    data() {
        return {
            buyNum: 1,
            priceStr: '',
            selectedSpec: [],
            specId: undefined
        }
    },
    methods: {
        getSpecItem() {
            if (this.spec.length == 0) {
                return null
            }
            let specStr = ''
            for (let i = 0; i < this.spec.length; i++) {
                let val = this.selectedSpec[i]
                if (val) specStr += ' ' + val
                else return null
            }
            specStr = specStr.trim()
            for (let i = 0; i < this.specItems.length; i++) {
                if (this.specItems[i].des === specStr) {
                    return this.specItems[i]
                }
            }
            return null
        },
        specClick(index, val) {
            this.selectedSpec[index] = val
            let specItem = this.getSpecItem()
            if (specItem) {
                this.stock = specItem.stock
                this.price = specItem.price
            }

        },
        // submit order
        submit() {
            let url = `/cart/add?gid=${this.goodsId}&num=${this.buyNum}`
            if (this.spec.length === 0) {
                window.location = url
                return
            }
            let specItem = this.getSpecItem()
            if (!specItem) {
                for (let i = 0; i < this.spec.length; i++) {
                    if (!this.selectedSpec[i]) {
                        showToast("请选择" + this.spec[i].name)
                        break
                    }
                }
                return
            }
            url += `&specId=${specItem.id}`
            window.location = url
        }
    },
    setup(props) {
        return props
    },
    watch: {
        price: {
            handler(newValue, oldValue) {
                this.priceStr = priceFormat(newValue)
            },
            immediate: true
        }
    }
}
