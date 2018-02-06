/**
 * Component for monitoring scrolling, can be used
 * to support infinite scroll.
 * Requires main scroll element to have id="scrollWatch".
 *
 * The scrollWatch element is listened for scroll, it child must not change
 * as it is used to calculate the document size.
 *
 * TODO CONSIDER Use directive instead of HTML ID ?
 *
 * There is an issue if all fits to monitor. In such case then no scroll
 * event is fired and we can not load more data. This can be solved
 * by using the lp-scroll-watch-directive.
 *
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["angular"], definition);
    }
})((angular) => {

    let scrollWatch;
    let scrollWatchChild;
    const callbacks = [];

    /**
     * Can be used at the bottom of the list to inform user about
     * number of visible elements or loading more elements on request (click)
     * which can solve issues with bigger monitors then initial size
     * of the list.
     */
    function directive() {

        function link($scope, element, attrs) {
            $scope.onLoadMore = () => callCallbacks(true);
        }

        return {
            "restrict": "E",
            "scope": {
                "total": "<",
                "limit": "<"
            },
            "templateUrl": "app/modules/lp-scroll-watch-list-tail.html",
            "link": link
        }
    }

    /**
     * Enable user to register/unregister events connected to scroll and
     * update scroll binding to HTML.
     */
    function service() {

        // Child of scrollWatch can change, so we take care of that here.
        this.updateReference = () => {
            scrollWatchChild = scrollWatch.firstElementChild;
        };

        this.registerCallback = (callback) => {
            callbacks.push(callback);
            return callback;
        };

        this.unRegisterCallback = (callback) => {
            const index = callbacks.indexOf(callback);
            if (index == -1) {
                console.warn("Removing unregistered callback!", new Error());
            }
            callbacks.splice(index, 1);
        };

    }

    function init() {
        const module = angular.module("lp-scroll-watch", []);
        module.service("$lpScrollWatch", service);
        module.directive("lpScrollWatchListTail", directive);

        angular.element(document).ready(() => {
            scrollWatch = document.getElementById("scrollWatch");
            scrollWatch.addEventListener("scroll", onScroll);
        });
    }

    function onScroll() {
        const scrollBottom = scrollWatch.scrollTop + scrollWatch.offsetHeight;
        const documentSize = scrollWatchChild.scrollHeight;
        // Load almost one-page in front.
        const startLoadingAtPxFromBottom = scrollWatch.offsetHeight * 0.75;
        const scrollLimit = documentSize - startLoadingAtPxFromBottom;
        if (scrollBottom >= scrollLimit) {
            callCallbacks(false);
        }
    }

    function callCallbacks(byButton) {
        callbacks.forEach(callback => callback(byButton));
    }

    init();
});