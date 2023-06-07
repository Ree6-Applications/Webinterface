export interface DataType<T> {
    type: string;
    visible: boolean;
    name: string;
    jsonName: string;
    value: T;
    unit: string;
}

export interface Model {
    primaryIcon: string;
    name: string;
    model: DataType<any>[];
    isModel: (json: any) => boolean;
    renderFormat: (json: any) => string;
}

export function model2JSON(model: DataType<any>[]): string {
    let json: string = '{';
    model.forEach((data: DataType<any>) => {

        if(data.type == "string" || data.type == "int" || data.type == "selector") {
            json += `"${data.jsonName}": "${data.value}",`;
        } else {
            json += `"${data.jsonName}": ${data.value == null ? null : '"' + data.value.id + '"'},`;
        }
    });
    json = json.slice(0, -1);
    json += '}';
    return json;
}