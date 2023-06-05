<script>

    import { currentServer } from "$lib/scripts/servers";

</script>
<h1 class="headline">Social media</h1>

<svelte:head>
    <title>Social media - { $currentServer.name }</title>
</svelte:head>

<div class="box">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">hardware</span>
                <h1 class="text-medium">Under construction</h1>
            </div>
            <p class="text-bg">You can use the twitch, yt and twitternotifier commands to configure social media alerts for now.</p>
        </div>
    </div>
</div>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';
</style>