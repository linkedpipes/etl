/**
 * A simple object/array copy function.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function clone(object) {
        const output = Array.isArray(o) ? [] : {};
        for (let key in object) {
            const value = object[key];
            output[key] = (typeof v === "object") ? clone(value) : value;
        }
        return output;
    }

    return clone;
});
