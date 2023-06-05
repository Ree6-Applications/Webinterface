import { writable, type Writable } from "svelte/store";
import { get, post_js } from "./constants";

export type Setting = {
    name: string,
    displayName: string,
    value: Writable<string>
}

export let settingsLoading = writable(true);
let settings: Map<string, Setting> = new Map<string, Setting>();

export function createSetting(name: string, displayName: string, value: string) {

    if(settings.has(name)) {
        let setting = settings.get(name)!;
        setting.displayName = displayName;
        setting.value.set(value);

        settings.set(name, setting)
        return;
    }

    settings.set(name, {
        name: name,
        displayName: displayName,
        value: writable(value)
    });
}

export function setting(name: string): Setting {

    if (!settings.has(name)) {
        settings.set(name, {
            name: name,
            displayName: name,
            value: writable(":loading")
        });
    }

    return settings.get(name)!;
}

export async function updateSetting(name: string, guild: string, value: string) {
    if(value === ":loading") {
        return;
    }

    const setting = settings.get(name)!;
    setting.value.set(":loading")

    const json = await post_js("/settings/" + guild + "/" + name + "/update", JSON.stringify({
        "value": value
    }))

    if(!json.success) {
        return;
    }

    setting.value.set(value);
    settings.set(name, setting);
}

export function allSettings(prefix: string): Map<string, Setting> {

    const map = new Map<string, Setting>();

    settings.forEach((value, key) => {
        if (key.startsWith(prefix)) {
            map.set(key, value);
        }
    });

    return map;
}

export async function loadSettings(guild: string) {
    settingsLoading.set(true);

    const res = await get("/settings/"  + guild + "/");

    if (res.status != 200) {
        return;
    }

    const data = await res.json();
    console.log(data);
    const objects: any[] = data.object;
    
    objects.forEach((obj) => {
        createSetting(obj.name, obj.displayName, obj.value)
    });

    settingsLoading.set(false);
}