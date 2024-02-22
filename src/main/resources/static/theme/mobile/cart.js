export default {
    props: {},
    data() {
        return {
            sumPrice: 0,
            sumPriceStr: '0',
            goods: []
        }
    },
    methods: {
        add(goodsId, specId) {
            this.getCart(`/cart/json?m=add&goodsId=${goodsId}&specId=${specId}&num=1`)
        },
        del(goodsId, specId) {
            this.getCart(`/cart/json?m=del&goodsId=${goodsId}&specId=${specId}`)
        },
        sub(goodsId, specId) {
            this.getCart(`/cart/json?m=sub&goodsId=${goodsId}&specId=${specId}&num=1`)
        },
        getCart(url) {
            fetch(url).then(response => response.json())
                .then(data => {
                    this.goods = data
                    this.goods.forEach(item => {
                        item.goodsPriceStr = priceFormat(item.goodsPrice)
                        item.sumPriceStr = priceFormat(item.goodsPrice * item.num)
                        this.sumPrice += item.goodsPrice * item.num
                    })
                    console.log(this.goods)
                }).catch(error => {
                showToast("系统错误")
                console.error('Error:', error)
            });
        },
        toBuy() {
            window.location = "/cart/buy"
        }
    },
    mounted() {
        this.getCart('/cart/json?w=100')
    },

    setup(props) {
        return props
    },
    watch: {
        sumPrice: {
            handler(newValue) {
                this.sumPriceStr = priceFormat(newValue)
            },
            immediate: true
        }
    }
}