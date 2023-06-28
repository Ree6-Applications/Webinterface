export interface DataType<T> {
    type: string;
    visible: boolean;
    name: string;
    jsonName: string;
    value: T;
    unit: string;
}

export interface ConfigurableDataType<T> extends DataType<T> {
    jsonResName: string;
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
            json += `"${data.jsonName}":"${data.value}",`;
        } else {
            json += `"${data.jsonName}":${data.value == null ? null : '"' + data.value.id + '"'},`;
        }
    });
    json = json.slice(0, -1);
    json += '}';
    return json;
}

export function jsonIntoModel(model: ConfigurableDataType<any>[], json: any) {

    const entries = Object.entries(json);
    model.forEach((data: ConfigurableDataType<any>) => {
        if(data.type == "string" || data.type == "int" || data.type == "selector") {
            data.value = (entries.find((entry: any) => entry[0] == data.jsonResName) ?? ["", null])[1]
        } else if(data.type == "channel") {

            const value: any = (entries.find((entry: any) => entry[0] == data.jsonResName) ?? ["", null])[1];

            data.value = value == null ? null : {
                id: value.id,
                name: value.name,
                type: value.type
            }

            //data.value = entries.find((entry: any) => entry[0] == data.jsonName) == undefined ? null : entries.find((entry: any) => entry[0] == data.jsonName);
        }
    });

    return model;
}