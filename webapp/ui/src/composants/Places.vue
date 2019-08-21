<template>
    <input v-bind:id="id" type="text" v-bind:class="[options.style ? 'ap-input' : 'ap-nostyle-input']"
           v-bind:value="value"
           v-on:input="$emit('input', $event.target.value)"/>
</template>

<script>
    import places from 'places.js';

    export default {
        props: {
            id: {
                value: String,
                default: null
            },
            value: {
                type: String,
                default: null
            },
            options: {
                type: Object,
                default: function () {
                    return {};
                }
            }
        },
        data: function() {
            return {
                places: null
            }
        },
        mounted: function() {
            this.places = places(Object.assign({
                container: document.getElementById(this.id),
                type: 'city',
                aroundLatLngViaIP: false,
                style: false,
                useDeviceLocation: false,
                language: 'fr',
                countries: ['fr'],
                templates: {
                    value: function(suggestion) {
                        return suggestion.name;
                    }
                }
            }, this.options));

            var self = this;
            this.places.on('change', function(e) {
                self.$emit('change', e.suggestion);
            });
            this.places.on('clear', function() {
                self.$emit('clear');
            });
        }
    }
</script>