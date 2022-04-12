<template>
	<div class="search-result" :style="{
			top: `calc(45% - ${35 * this.topPosition}%)`,
			left: `calc(50% - ${35 * this.leftPosition}%)`,
		}" @click="click">
		<div class="result-distance" title="Distance of this result from the query">
			<!-- Show how far away the result is from the original query -->
			<span>{{ this.classification }} - {{ Math.floor(this.distance * 100) / 100 }}</span>
		</div>
		<div class="title" :style="{
			backgroundColor: this.getColor(this.classification),
		}">
			<!-- The title of the search result -->
			{{ this.title }}
		</div>
		<div class="context">
			{{ this.context }}
		</div>

		<!-- Debug information -->
		<div class="debug">
			<ul>
				<li>TopPosition: {{ this.topPosition }}</li>
				<li>LeftPosition: {{ this.leftPosition }}</li>
				<li>Class: {{ this.classification }}</li>
			</ul>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
	name: 'SearchResult',
	props: {
		/* Now if only I knew what the fuck the backend throws at me... */
		topPosition: Number,
		leftPosition: Number,

		title: String,
		context: String,
		classification: Number,
		distance: Number,
	},
	methods: {
		click() {
			// Open the search result in a new tab
			// Url is the title of the wikipedia article
			window.open(`https://en.wikipedia.org/wiki/${this.title}`, '_blank');
		},
		getColor() {
			// Get the color of the class
			return `hsl(${(this.classification || 0) * 360 / 12}, 40%, 50%)`;
		},
	}

});
</script>

<style scoped>
.search-result {
	width: 20vw;
	height: auto;
	max-height: 15vh;
	overflow: hidden;
	text-overflow: ellipsis;

	margin: 0;
	padding: 0;

	position: absolute;
	/* top: 50px;
	left: 50px; */

	background-color: var(--background-box-color);
	border-radius: 0.3em;
	box-shadow: var(--box-shadow);

	transition: 0.4s;

	transform: translate(-50%, -50%);

	/* Change cursor on hover to a hand */
	cursor: pointer;
}

.context {
	margin: 0.2em;
	text-overflow: ellipsis;
	overflow: hidden;
}

.result-distance {
	font-size: 10px;
	position: absolute;
	top: 0;
	right: 0;
	padding: 0.25em;
}

.search-result:hover {
	box-shadow: var(--box-shadow-hover);
	z-index: 100;
}

.title {
	text-align: center;
	font-size: 1.2em;
	font-weight: 800;

	background-color: #444;
}

.debug {
	display: none;
}
</style>
