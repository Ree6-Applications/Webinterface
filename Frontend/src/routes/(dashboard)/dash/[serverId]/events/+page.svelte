<script lang="ts">
    import { page } from "$app/stores";
    import ChannelSelector from "$lib/components/settings/channelSelector.svelte";
    import MassBoolean from "$lib/components/settings/massBoolean.svelte";
    import MassRoleSelector from "$lib/components/settings/massRoleSelector.svelte";
    import MessageSelector from "$lib/components/settings/messageSelector.svelte";

</script>

<h1 class="headline">Logging</h1>

<ChannelSelector icon="tag" title="Logging channel" description="Select the channel for all log messages." endpoint={"/guilds/" + $page.params.serverId + "/log"} />

<MassBoolean title="Logging features" description="Configure all events that should be logged." prefix="logging_" icon="dynamic_feed" />

<h1 class="headline">Join settings</h1>

<MassRoleSelector title="Automatic roles" description="Configure all roles that should be given to users when joining the guild." icon="military_tech"
    endpoint={"/guilds/" + $page.params.serverId + "/autorole"}  />

<div class="default-margin"></div>

<ChannelSelector icon="rocket_launch" title="Welcome channel" description="Select the channel for welcoming people." endpoint={"/guilds/" + $page.params.serverId + "/welcome"} />

<div class="default-margin"></div>

<MessageSelector icon="mail" title="Welcome message" description="Configure the welcome message." settingName="message_join" 
formattingDirectives="%guild_name% - Name of the guild %nl %user_mention% - @<username> to mention the user"/>

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