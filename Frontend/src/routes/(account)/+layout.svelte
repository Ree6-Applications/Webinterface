<script lang="ts">
  interface Props {
    children?: import('svelte').Snippet;
  }

  let { children }: Props = $props();
  
  </script>
  <div class="body">
      <div class="content">
          {@render children?.()}
      </div>
  </div>
  
  <style lang="scss">
      .body {
          padding: 0;
          margin: 0;
          display: flex;
          height: 100vh;
          width: 100vw;
          background-color: var(--onyx);
          display: flex;
          align-items: center;
          justify-content: center;
      }

  </style>