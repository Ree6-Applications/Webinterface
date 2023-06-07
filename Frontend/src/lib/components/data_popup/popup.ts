export interface DataType<T> {
    type: string;
    visible: boolean;
    name: string;
    jsonName: string;
    value: T;
}

export interface Model {
    primaryIndex: number;
    primaryIcon: string;
    name: string;
    model: DataType<any>[];
}