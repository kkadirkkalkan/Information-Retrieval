<template>
	<div class="example">
		{{ msg }}
	</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'Example',
	props: {
		msg: String,
	},
})
</script>

<style scoped>
.example {
	font-size: 1em;
	font-weight: 800;
	font: 'Avenir', Helvetica, Arial, sans-serif;
}

</style>
