<script lang="ts">
    import { page } from "$app/stores";
    import DataPopup from "$lib/components/data_popup/dataPopup.svelte";
    import MassBoolean from "$lib/components/settings/massBoolean.svelte";
    import MassDataSelector from "$lib/components/settings/massDataSelector.svelte";
    import MassStringSelector from "$lib/components/settings/massStringSelector.svelte";
    import StringSelector from "$lib/components/settings/stringSelector.svelte";
    import { currentServer } from "$lib/scripts/servers";
    import Warnings from "./warnings.svelte";

    
    let blacklist = ["Glatze", "Bastard"]

    let wordToAdd: string = ""

    function addWord() {
        blacklist.push(wordToAdd)
        blacklist = blacklist
        wordToAdd = ""
    }

    function removeWord(toRemove: string) {
        blacklist = blacklist.filter(word => word != toRemove)
        blacklist = blacklist
    }

</script>

<svelte:head>
    <title>Moderation - {$currentServer.name}</title>
</svelte:head>

<h1 class="headline">Moderation settings</h1>

<StringSelector icon="layers" title="Command prefix" description="Select the prefix for all commands." settingName="chatprefix" />

<div class="default-margin"></div>

<MassStringSelector icon="block" title="Blacklisted words" description="Any message containing a word from the list of blacklisted words will be deleted and logged." endpoint={"/guilds/" + $page.params.serverId + "/blacklist"} />

<h1 class="headline">Command settings</h1>

<div class="default-margin"></div>

<MassBoolean icon="keyboard_command_key" title="Enabled commands" description="Configure all commands that can be executed using the discord bot." prefix="command_" />

<h1 class="headline">Warnings & punishments</h1>

<div class="default-margin"></div>

<Warnings />

<div class="default-margin"></div>

<MassDataSelector icon="auto_awesome" title="Automatic punishments" description="Automatically do certain things when a user has more than a certain amount of warnings."
    models={[
        {
            name: "Timeout",
            primaryIcon: "timelapse",
            isModel: (json) => json.action == "1",
            renderFormat: (json) => "More than " + json.neededWarnings + " warnings result in a timeout for " + json.timeoutTime + " seconds.",
            model: [
                {
                    name: "Needed warnings",
                    jsonName: "neededWarnings",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "warnings",
                },
                {
                    name: "action",
                    jsonName: "action",
                    type: "string",
                    value: "1",
                    visible: false,
                    unit: ""
                },
                {
                    name: "Timeout length",
                    jsonName: "timeoutTime",
                    type: "int",
                    value: 1000,
                    visible: true,
                    unit: "seconds"
                },
                {
                    name: "role",
                    jsonName: "roleId",
                    type: "role",
                    value: null,
                    visible: false,
                    unit: ""
                },
            ]
        },
        {
            name: "Role",
            primaryIcon: "military_tech",
            isModel: (json) => json.action == "2" || json.action == "3",
            renderFormat: (json) => "More than " + json.neededWarnings + " warnings result in " + (json.action == "2" ? "adding the '" + json.role.name + "' role to" : "removing the '" + json.role.name + "' role from") + " the user.",
            model: [
                {
                    name: "Needed warnings",
                    jsonName: "neededWarnings",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "warnings",
                },
                {
                    name: "Role action",
                    jsonName: "action",
                    type: "selector",
                    value: "2",
                    visible: true,
                    unit: "2:Add role,3:Remove role"
                },
                {
                    name: "Timeout length",
                    jsonName: "timeoutTime",
                    type: "int",
                    value: 0,
                    visible: false,
                    unit: "seconds"
                },
                {
                    name: "Role",
                    jsonName: "roleId",
                    type: "role",
                    value: null,
                    visible: true,
                    unit: ""
                },
            ]
        },
        {
            name: "Punishment",
            primaryIcon: "gavel",
            isModel: (json) => json.action == "4" || json.action == "5",
            renderFormat: (json) => "More than " + json.neededWarnings + " warnings result in a " + (json.action == "4" ? "kick" : "ban") + ".",
            model: [
                {
                    name: "Needed warnings",
                    jsonName: "neededWarnings",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "warnings",
                },
                {
                    name: "Punishment type",
                    jsonName: "action",
                    type: "selector",
                    value: "4",
                    visible: true,
                    unit: "4:Kick,5:Ban"
                },
                {
                    name: "Timeout length",
                    jsonName: "timeoutTime",
                    type: "int",
                    value: 0,
                    visible: false,
                    unit: "seconds"
                },
                {
                    name: "Role",
                    jsonName: "roleId",
                    type: "role",
                    value: null,
                    visible: false,
                    unit: ""
                },
            ]
        }
    ]}
endpoint={"/guilds/" + $page.params.serverId + "/warnings/punishments"} deleteField={(json) => json.punishmentId}/>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';
    @import '$lib/styles/chips.scss';

    .chip-button {
        margin-left: 0.1rem;
        color: var(--seasalt);
        transition: 250ms ease;

        &:hover {
            color: var(--primary);
        }
    }

</style>