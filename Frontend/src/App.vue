<template>
	<!-- Main entry point for the search engine app -->
	<div class="app">
		<!-- Debug input box for the baseURL of axios -->
		<div class="url-debugger">
			<input type="text" v-model="baseURL" />
		</div>


		<!-- The search bar -->
		<div class="search-bar">
			<div class="search-bar-text">
				Search for something like <code>cats</code> or <code>information retrieval</code>.
			</div>
			<input 
				type="text" 
				v-model="searchQuery" 
				@keyup.enter="search" 
				placeholder="Search..." 
			/>
		</div>
		<div class="search-results" v-if="this.searchResults.length > 0">
			<div class="search-result" v-for="(result, index) in this.searchResults" :key="result.title">
				<SearchResult
					:topPosition="this.getPositionAroundCircle(index, this.searchResults.length).y"
					:leftPosition="this.getPositionAroundCircle(index, this.searchResults.length).x"

					:title="result.title"
					:context="result.context"
					:classification="result.classification"

					:distance="this.getEuclideanDistance(result.vector, this.queryVector)"
				/>
			</div>
		</div>

	</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SearchResult from '@/components/SearchResult.vue';

import axios from 'axios';

type BackendResult = {
	title: string; // Title of the document 
	context: string; // Snippet of the document
	//url: string; // URL of the document
	classification: number, // Class of the document
	vector: number[]; // Vector representation of the document
}

export default defineComponent({
	name: 'App',
	components: {
		SearchResult
	},
	data() {
		return {
			searchQuery: '', // Search query linked to the input field with v-model
			searchResults: [] as BackendResult[], // Search results returned from backend, **TODO** defined type
			queryVector: [] as number[], // Vector representation of the search query

			baseURL: 'http://localhost:1234/', // Base URL of the backend
		}
	},
	methods: {
		search() {
			axios.get('/search', {
				baseURL: this.baseURL,
				params: {
					query: this.searchQuery
				}
			}).then(response => {
				console.log(response.data);
				this.searchResults = response.data.documents;
				this.queryVector = response.data.query.vector;
			}).catch(error => {
				this.searchResults = [
					{
						title: 'An error occured',
						context: 'An error occured while searching for your query. Please try again later.\n' + error,
						classification: 0,
						vector: []
					}
				]
			});
		},
		searchMock() {
			// Query the backend for search results

			// IF ONLY IT EXISTED
			const makeMockResult = (n: number): BackendResult => {
				return {
					title: 'Mock title ' + n,
					context: 'Mock preview ' + n,
					classification: n,
					vector: [n, n-1, 0]
				};
			}

			this.queryVector = [2, 2, 2]; // Whatever, just for testing

			// Mock backend response
			this.searchResults = Array.from({length: 10}, (_, i) => makeMockResult(i));
		},
		getEuclideanDistance(a: number[], b: number[]): number {
			let sum = 0;
			for (let i = 0; i < a.length; i++) {
				sum += Math.pow(a[i] - b[i], 2);
			}
			return Math.sqrt(sum);
		},
		getPositionAroundCircle(index: number, total: number): {x: number, y: number} {
			const angle = (index / total) * 2 * Math.PI;

			return {
				x: Math.cos(angle),
				y: Math.sin(angle),
			}
		}
	},
});
</script>

<style>
:root {
	--background-color: #111;
	--background-box-color: #222;

	--text-color: #fff;

	--box-shadow: 0.15em 0.15em 0.15em 0.15em #000;
	--box-shadow-hover: 0.1em 0.1em 0.1em 0.1em #666;
}

html, body, #app, .app {
	height: 100%;
	padding: 0;
	margin: 0;
}

body {
	--webkit-font-smoothing: antialiased;
	--moz-osx-font-smoothing: grayscale;

	font-family: Avenir, Helvetica, Arial, sans-serif;
	font-weight: 400;

	background-image: url('~@/assets/boxed-bg-dark.jpg');
	background-color: var(--background-color);
	color: var(--text-color);
}

#app {
	font-family: Avenir, Helvetica, Arial, sans-serif;
	-webkit-font-smoothing: antialiased;
	-moz-osx-font-smoothing: grayscale;
	text-align: center;

	overflow: hidden;
}

/* Component styles */

input {
	width: calc(100% - 1em);

	margin: 0 auto;
	padding: 0.5em;

	border: none;
	border-radius: 0 0.5em;

	background-color: #ddd;
	color: #000;

	font-size: 1em;

	transition: 0.4s;
}

input:focus {
	border-radius: 0.5em 0;
}

/* Actual content below */

.search-bar {
	width: 30vw;
	height: fit-content;

	margin: auto;
	padding: 0.5em;

	/* Vertically center div */
	position: relative;
	top: 45%;
	transform: translateY(-50%);

	background-color: var(--background-box-color);

	border-radius: 0.3em;
	box-shadow: var(--box-shadow);
}
.search-bar-text {
	font-size: 1.4em;
	padding: 0.25em;
}

.url-debugger {
	position: absolute;
	top: 0;
	left: 0;
	font-size: 0.8em;
	color: #999;

}
</style>
