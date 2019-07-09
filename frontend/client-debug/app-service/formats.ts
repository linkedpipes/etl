export function asHumanReadableSize(size: number): string {
    if (size < 1024) {
        return size + " B";
    }
    size = size / 1024;
    if (size < 1024) {
        return size.toFixed(2) + " kB";
    }
    size = size / 1024;
    return size.toFixed(2) + " MB";
}