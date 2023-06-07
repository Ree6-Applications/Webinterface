export interface DataType<T> {
    type: string;
    visible: boolean;
    name: string;
    jsonName: string;
    value: T;
    unit: string;
}

export interface Model {
    primaryIndex: number;
    primaryIcon: string;
    name: string;
    jsonName: string;
    jsonInserter: string;
    model: DataType<any>[];
}

export function model2JSON(model: DataType<any>[]): string {
    let json: string = '{';
    model.forEach((data: DataType<any>) => {

        if(data.type == "string" || data.type == "int") {
            json += `"${data.jsonName}": "${data.value}",`;
        } else {
            json += `"${data.jsonName}": ${data.value == null ? null : '"' + data.value.id + '"'},`;
        }
    });
    json = json.slice(0, -1);
    json += '}';
    return json;
}