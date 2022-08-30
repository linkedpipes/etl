type IdentifierFunction<T> = (item: T) => string;

type FilterFunction<T> = (item: T) => boolean;

type CompareFunction<T> = (left: T, right: T) => number;

export interface InMemoryRepository<T> {

  /**
   * Return true if data are ready in the repository.
   */
  isReady(): boolean;

  /**
   * True if data has been set, but they are empty.
   */
  isEmpty(): boolean;

  /**
   * Data visible to the user.
   */
  getVisibleData(): T[];

  /**
   * Total number of data entries passing current filters.
   */
  getDataSize(): number;

  /**
   * Set repository data.
   */
  setData(data: T[]): void;

  /**
   * Replace filter function.
   */
  setFilterFunction(filterFunction: FilterFunction<T>): void;

  /**
   * Replace ordering function.
   */
  setOrderFunction(compareFunction: CompareFunction<T>): void;

  /**
   * Increase visible data limit.
   */
  showMore(count: number): void;

  /**
   * Remove items with given identifier.
   */
  removeItems(identifier: string[]): void;

  /**
   * Add or update given items.
   */
  updateItems(changedData: T[]): void;

}

class DefaultInMemoryRepository<T> implements InMemoryRepository<T> {

  private visibleData: T[] = [];

  private visibleLimit: number;

  private ready: boolean = false;

  private data: T[] = [];

  private readonly identifierFunction: IdentifierFunction<T>;

  private filterFunction: FilterFunction<T> = (T) => true;

  private compareFunction: CompareFunction<T> = (left, right) => 0;

  constructor(
    identifierFunction: IdentifierFunction<T>,
    visibleLimit: number = 10) {
    this.identifierFunction = identifierFunction;
    this.visibleLimit = visibleLimit;
  }

  isReady(): boolean {
    return this.ready;
  }

  isEmpty(): boolean {
    return this.data.length === 0;
  }

  getVisibleData(): T[] {
    return this.visibleData;
  }

  getDataSize(): number {
    return this.data.length;
  }

  setData(data: T[]): void {
    this.ready = true;
    this.data = data;
    this.recomputeVisibleData();
  }

  private recomputeVisibleData(): void {
    console.log("recomputeVisibleData", this.data);
    this.visibleData = this.data
      .filter(this.filterFunction)
      .slice(0, this.visibleLimit)
      .sort(this.compareFunction);
  }

  setFilterFunction(filterFunction: (item: T) => boolean): void {
    this.filterFunction = filterFunction;
    this.recomputeVisibleData();
  }

  /**
   * Replace ordering function.
   */
  setOrderFunction(compareFunction: (left: T, right: T) => number): void {
    this.compareFunction = compareFunction;
    this.recomputeVisibleData();
  }

  showMore(count: number): void {
    this.visibleLimit += count;
  }

  removeItems(identifier: string[]): void {
    const removedData = identifier.map(this.removeItemFromData);
    this.onItemsChanged(removedData);
  }

  private removeItemFromData(identifier: string): (T | undefined) {
    const dataIndex = this.find(this.data, identifier);
    if (dataIndex === -1) {
      return undefined;
    }
    this.data[dataIndex] = this.data[this.data.length - 1];
    return this.data.pop();
  }

  private find(items: T[], identifier: string): number {
    for (let index = 0; index < items.length; ++index) {
      if (this.identifierFunction(items[index]) === identifier) {
        return index;
      }
    }
    return -1;
  }

  /**
   * Check if any of the items is visible and if so trigger visible
   * data computation.
   */
  private onItemsChanged(items: (T | undefined)[]): void {
    for (const item of items) {
      if (item === undefined) {
        continue;
      }
      if (this.filterFunction(item)) {
        this.recomputeVisibleData();
        return;
      }
    }
  }

  updateItems(changedData: T[]): void {
    for (const item of changedData) {
      const identifier = this.identifierFunction(item);
      const index = this.find(this.data, identifier);
      if (index === -1) {
        this.data.push(item);
      } else {
        this.data[index] = item;
      }
    }
    this.onItemsChanged(changedData);
  }

}

export function createInMemoryRepository<T>(
  identifierFunction: IdentifierFunction<T>,
  visibleLimit: number = 10
): InMemoryRepository<T> {
  return new DefaultInMemoryRepository<T>(identifierFunction, visibleLimit);
}
