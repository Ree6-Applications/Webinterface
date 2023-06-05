import { writable } from "svelte/store";
import { BASE_PATH, get } from "./constants";

export interface Server {
    icon: string;
    name: string;
    setup: boolean;
    id: number;
}

export interface Channel {
    id: string | null;
    name: string | null;
    type: string;
}

export interface Role {
    id: string;
    name: string;
    color: number;
}

export let serversLoading = writable(true);
export let serverError = writable(false);

export let currentLoading = writable(true);
export let currentError = writable(false);
export let currentChannels = writable<Channel[]>([]);
export let currentRoles = writable<Role[]>([]);

export let currentServer = writable<Server>({
    icon: "",
    name: "",
    setup: false,
    id: 0
});

export let servers: Map<string, Server> = new Map<string, Server>();

export function server(name: string): Server {
    return servers.get(name)!;
}

export async function loadServers() {
    serversLoading.set(true);

    const res = await get("/guilds/");
    const json = await res.json();
    console.log(json);

    if(json.success) {
        for(let server of json.object) {
            servers.set(server.id, {
                icon: server.iconUrl,
                name: server.name,
                setup: server.hasBot,
                id: server.id
            });
        }
    } else {
        location.assign(BASE_PATH + "/auth/discord/request")
        return;
    }

    serversLoading.set(false);

}