const SearchForm = {
    data() {
        return {
            bgCls: 'hidden',
            q: ''
        }
    },
    mounted() {
        this.bgRef.style = ""
    },
    methods: {
        show() {
            this.bgCls = 'search-bg-show'
            this.inputRef.style.backgroundColor = '#f7f7f7'

        },
        hidden() {
            this.bgCls = 'hidden'
            this.q = ''
            this.inputRef.style.backgroundColor = 'white'
        },
        submit(){
            window.location = "/list?q=" + this.q
        }
    },
    setup() {
        return {
            bgRef: null,
            inputRef: null
        }
    }
}
Vue.createApp(SearchForm).mount('#openSearch')