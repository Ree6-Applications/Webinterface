export interface DataType<T> {
    type: string;
    visible: boolean;
    name: string;
    value: T;
}