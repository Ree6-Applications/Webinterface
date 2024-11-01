<script lang="ts">
  
  </script>
  <div class="body">
      <div class="content">
          <slot />
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