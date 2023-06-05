<script lang="ts">
    import { onMount } from "svelte";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import RolePicker from "../rolePicker.svelte";
    import { get, post } from "$lib/scripts/constants";
    import { slide } from "svelte/transition";
    import type { Role } from "$lib/scripts/servers";

    export let icon: string;
    export let title: string;
    export let windowTitle: string = "Select a role to add.";
    export let description: string;
    export let endpoint: string;

    let roles: Role[] = [];
    let roleLoading: string | null = null;
    let rolePicker = false;
    let error = false;
    let loading = true;

    onMount(async () => {

        // Request value
        const res = await get(endpoint);

        if(res.status != 200) {
            error = true;
            console.log(res)
            return;
        }

        const json = await res.json();
        console.log(json)
        json.object.forEach((element: any) => {
            roles.push({
                id: element.id,
                name: element.name,
                color: element.color
            });
        });

        error = false;
        loading = false;

    })
</script>

{#if rolePicker}
<RolePicker message={windowTitle} callback={async (role) => {
    if(loading) return;
    rolePicker = false

    if(role != null) {

        for(let element of roles) {
            if(element.id == role.id) return;
        }

        loading = true;

        // Add role
        const res = await post(endpoint + "/add", JSON.stringify({
            "value": role.id
        }))

        if(res.status != 200) {
            console.log(res)
            error = true;

            setTimeout(() => {
                loading = false;
                error = false;
            }, 500);
            return;
        }

        const json = await res.json();
        console.log(json)

        if(!json.success) {
            error = true;

            setTimeout(() => {
                loading = false;
                error = false;
            }, 500);
            return;
        }

        loading = false;

        roles.push(role);
        roles = roles;
    }
    
}} current={null} />
{/if}

<div class="box">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">{icon}</span>
                <h1 class="text-medium">{title}</h1>
            </div>
            <p class="text-bg">{description}</p>
        </div>

        {#if loading}
        <div class="loading">
            <LoadingIndicator error={error} size="45" />
        </div>

        {:else}

        <div class="button-bar">
            <div on:click={() => {
                rolePicker = true
            }} on:keydown class="button icon-button">
                <span class="material-icons icon-small icon-primary">add</span>
            </div>
        </div>

        {/if}
    </div>

    {#if !loading && roles.length > 0}
    <div in:slide class="chips default-margin">
        {#each roles as role}
        <div class="chip">
            <p class="text-small">{role.name}</p>

            {#if roleLoading != role.id}
            <span on:click={async () => {
                if(roleLoading != null) return;
                roleLoading = role.id;

                // Delete role
                const res = await post(endpoint + "/remove", JSON.stringify({
                    "value": role.id
                }))

                if(res.status != 200) {
                    console.log(res)
                    roleLoading = null;
                    return;
                }
                
                const json = await res.json();
                console.log(json)
                if(!json.success) {
                    roleLoading = null;
                    return;
                }

                // Remove role from list
                roles = roles.filter((element) => {
                    return element.id != role.id;
                })

                roleLoading = null;

            }} on:keydown class="material-icons icon-primary clickable chip-button">close</span>
            {:else}
            <LoadingIndicator size="1.5rem" />
            {/if}
        </div>
        {/each}
    </div>
    {/if}
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
    @import '$lib/default.scss';
    @import '$lib/styles/chips.scss';
</style>