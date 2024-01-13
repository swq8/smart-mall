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
        getCart() {
            fetch('/cart/json?w=100').then(response => response.json())
                .then(data => {
                    this.goods = data
                    this.goods.forEach(item => {
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
        this.getCart()
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