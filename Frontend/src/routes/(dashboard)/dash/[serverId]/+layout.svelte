<script lang="ts">
    import { get } from "$lib/scripts/constants";
    import { currentError, currentLoading, currentServer, currentChannels, currentRoles, type Role, type Channel } from "$lib/scripts/servers";
    import { loadSettings } from "$lib/scripts/settings";
    import { onDestroy } from "svelte";

    let sub = currentServer.subscribe(async (server) => {
        if(server.id == 0) return;
        currentLoading.set(true);

        const res = await get("/guilds/" + server.id)
        if(res.status != 200) {
            currentError.set(true)
            return;
        }

        const json = await res.json()

        if(!json.success) {
            currentError.set(true);
            return;
        }

        console.log(json)
        currentLoading.set(false);

        // Add channels
        currentChannels.set([])
        for(let channel of json.object.channels) {
            currentChannels.update((channels) => {
                channels.push({id: channel.id, name: channel.name, type: channel.type})
                return channels;
            })
        }

        // Add roles
        currentRoles.set([])
        for(let role of json.object.roles) {
            currentRoles.update((roles) => {
                roles.push({id: role.id, name: role.name, color: role.color})
                return roles;
            })
        }

        // Load settings
        loadSettings(server.id + "");
    })

    onDestroy(() => {
        sub()
    })

</script>

<slot />