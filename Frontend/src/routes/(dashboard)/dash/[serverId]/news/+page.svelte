<script>
  import { page } from "$app/stores";
  import BooleanSelector from "$lib/components/settings/booleanSelector.svelte";
  import MassDataSelector from "$lib/components/settings/massDataSelector.svelte";


    import { currentServer } from "$lib/scripts/servers";

</script>

<svelte:head>
    <title>Levels - { $currentServer.name }</title>
</svelte:head>

<h1 class="headline">News</h1>

<div class="default-margin"></div>

<BooleanSelector icon="newspaper" title="Publish news messages" description="Let Ree6 publish your news messages after you sent them!" settingName="configuration_autopublish" />

<div class="default-margin"></div>

<BooleanSelector icon="sync" title="Ree6 announcements" description="Receive announcements published by us." settingName="configuration_news" />

<h1 class="headline">Rewards</h1>

<MassDataSelector icon="mic" title="Voice level rewards" description="Add rewards for reaching certain voice levels."
    models={[
        {
            name: "Role reward",
            primaryIcon: "military_tech",
            isModel: (json) => json.level,
            renderFormat: (json) => "When becoming voice level " + json.level + ", the user gets the " + json.role.name + " role.",
            model: [
                {
                    name: "Needed level",
                    jsonName: "level",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "",
                },
                {
                    name: "Rewarded role",
                    jsonName: "role",
                    type: "role",
                    value: null,
                    visible: true,
                    unit: ""
                }
            ]
        }
    ]}
endpoint={"/guilds/" + $page.params.serverId + "/voicerole"} deleteField={(json) => json.level}/>

<MassDataSelector icon="message" title="Message level rewards" description="Add rewards for reaching certain chat message levels."
    models={[
        {
            name: "Role reward",
            primaryIcon: "military_tech",
            isModel: (json) => json.level,
            renderFormat: (json) => "When becoming chat level " + json.level + ", the user gets the " + json.role.name + " role.",
            model: [
                {
                    name: "Needed level",
                    jsonName: "level",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "",
                },
                {
                    name: "Rewarded role",
                    jsonName: "role",
                    type: "role",
                    value: null,
                    visible: true,
                    unit: ""
                }
            ]
        }
    ]}
endpoint={"/guilds/" + $page.params.serverId + "/chatrole"} deleteField={(json) => json.role.id}/>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';
</style>