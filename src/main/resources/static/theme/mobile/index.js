const SearchForm = {
    data() {
        return {
            bgCls: 'hidden',
        }
    },
    mounted() {
        this.bgRef.style = ""
    },
    methods: {
        show() {
            this.bgCls = 'search-bg-show'
            setTimeout(() => {
                this.inputRef.focus()
            }, 200)

        },
        hidden() {
            this.bgCls = 'hidden'
        },
    },
    setup() {
        return {
            bgRef: null,
            inputRef: null
        }
    }
}
Vue.createApp(SearchForm).mount('#openSearch')