package app.eluvio.wallet.network.api.authd

import app.eluvio.wallet.network.dto.NftResponse
import app.eluvio.wallet.network.dto.NftTemplateDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface GatewayApi : AuthdApi {
    @GET("apigw/nfts")
    fun getNfts(): Single<NftResponse>

    @GET("apigw/skus")
    fun getSkus(marketplace: String, sku: String): Single<NftTemplateDto>

    companion object {
        val fakeWeatherResponse = """
            {
              "additional_media": [],
              "additional_media_custom_css": "",
              "additional_media_display": "Media",
              "additional_media_sections": {
                "featured_media": [
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": null,
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/locations/FOXWeather_Pheonix_SingleMediaView.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "PqXhp4LS6JwrqDJoLYvdRX",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__BCFj4qKmoDx2ReeFyRMNxnrAVV6wk6kMb96eWgo5TEW1Pm2RPvwiRjV8S2hpgdU1uNb2AK7Grv/files/locations/FoxWeather_Phoenix_Vertical%20Poster%20V2.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__JxCAR5bCx4ZcngPRU5oLRMmiY2h2Uk1bf76D4URaCvKxVcWP8WpeX6zwRVhd8sPAe3By397eFY/files/locations/FoxWeather_Row1_Pheonix.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": "",
                      "lock_condition": "",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "media_file": null,
                    "media_link": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                    },
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Video",
                    "name": "Phoenix - FOX Weather (Live)",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/locations/FoxWeather_Phoenix_Vertical Poster V2.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": [{ "key": "location", "value": "phoenix" }]
                  },
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": null,
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/locations/FOXWeather_LA_SingleMediaView.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "Pkw7Kp7xgaseNCnyDFuprP",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__BCFj4qKmoDx2ReeFyRMNxnrAVV6wk6kMb96eWgo5TEW1Pm2RPvwiRjV8S2hpgdU1uNb2AK7Grv/files/locations/FoxWeather_LA_Vertical%20Poster%20V2.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__JxCAR5bCx4ZcngPRU5oLRMmiY2h2Uk1bf76D4URaCvKxVcWP8WpeX6zwRVhd8sPAe3By397eFY/files/locations/FoxWeather_Row1_LA.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": "",
                      "lock_condition": "",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "media_file": null,
                    "media_link": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                    },
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Video",
                    "name": "Los Angeles - FOX Weather (Live)",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/locations/FoxWeather_LA_Vertical Poster V2.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": [{ "key": "location", "value": "los angeles" }]
                  },
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": null,
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/WashingtonDC/FoxWeather_DC-background.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "M1vWyY2W29Sr67SR7SZDzH",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__JxCAR5bCx4ZcngPRU5oLRMmiY2h2Uk1bf76D4URaCvKxVcWP8WpeX6zwRVhd8sPAe3By397eFY/files/WashingtonDC/FoxWeather_DC-poster.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__LvgHvGmAKayU6ws6N8MvPFZwB7PVWbXPv2heKwLuXw35Lvqr1gc5a69SrkA5VQZiGhZU6qfR9p/files/WashingtonDC/FoxWeather_DC-16x9.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": "",
                      "lock_condition": "",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "media_file": null,
                    "media_link": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                    },
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Video",
                    "name": "Washington DC - FOX Weather (Live)",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                      },
                      "/": "./files/WashingtonDC/FoxWeather_DC-poster.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": [{ "key": "location", "value": "washington dc" }]
                  }
                ],
                "sections": [
                  {
                    "collections": [
                      {
                        "display": "Media",
                        "id": "N2phUZy83nyKwHN1oLw79P",
                        "media": [
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "VXVbTktcPYhXKqzfsHSfub",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__K4tVQCmR87h4x8e2uUNz2PRaDrF6zEF43mrKs6yngEGF3dQvfvmnKopCLUyAbN4vygoujwJ9pa/files/locations/FoxWeather_Row1_Pheonix.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "media_file": null,
                            "media_link": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                              },
                              "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                            },
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Video",
                            "name": "Phoenix - FOX Weather (Live)",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": []
                          },
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "4Vz2k4XVRRPoNi3iyfbfQ5",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__K4tVQCmR87h4x8e2uUNz2PRaDrF6zEF43mrKs6yngEGF3dQvfvmnKopCLUyAbN4vygoujwJ9pa/files/locations/FoxWeather_Row1_LA.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "media_file": null,
                            "media_link": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                              },
                              "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                            },
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Video",
                            "name": "Los Angeles - FOX Weather (Live)",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": [{ "key": "location", "value": "los angeles" }]
                          },
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "NK5ET1R8MRqaJGZDTUm3bD",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__EtSzHNdSiutEciL8ecD7jL1BjN914kzy1fs4AzFVMAU6FD457tgJpJBT2D9ghCDFdmdWBrAGPY/files/WashingtonDC/FoxWeather_DC-16x9.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "media_file": null,
                            "media_link": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                              },
                              "/": "/qfab/hq__W19fPp5MB8Sw2gyAthkDYJjA5Pdvr6c39K2tmZERJFHzsj4eGpFHk6xj4kR5Vi19cC6pU7hHU/meta/public/asset_metadata"
                            },
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Video",
                            "name": "Washington DC - FOX Weather (Live)",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": []
                          }
                        ],
                        "name": "Fox Weather",
                        "show_autoplay": false
                      }
                    ],
                    "id": "H8cgmKU6G19UkLkWTyR8wR",
                    "name": ""
                  }
                ]
              },
              "additional_media_type": "Sections",
              "address": "0xeb65174e4ed37a0b99b2f8d130ef84c7cc740264",
              "attributes": [{ "name": "series", "type": "text", "value": "true" }],
              "background_image": null,
              "copyright": "",
              "created_at": "",
              "creator": "Eluvio Live",
              "description": "Precise. Personal. Powerful. FOX Weather brings America’s Weather Team to you, serving local weather news and live weather updates.",
              "description_rich_text": "\u003cp\u003ePrecise. Personal. Powerful. FOX Weather brings America’s Weather Team to you, serving local weather news and live weather updates. Free and ad supported, the all-access pass brings you personalized weather, personalized content and exclusive rewards from your favorite US locations, and tailored for you.\u003c/p\u003e",
              "display_name": "FOX Weather All Access",
              "edition_name": "Premiere",
              "embed_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9aRRiqWozaD5Gdf3UbfcpWBYjy4hRDooHNe835FRJYVKDTgSW6A1vMrdi4CbDGPoTMT5HA1XQr\u0026m=\u0026ap=\u0026lp=",
              "external_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9aRRiqWozaD5Gdf3UbfcpWBYjy4hRDooHNe835FRJYVKDTgSW6A1vMrdi4CbDGPoTMT5HA1XQr\u0026m=\u0026ap=\u0026lp=",
              "generative": false,
              "has_audio": false,
              "id_format": "token_id",
              "image": "https://main.net955305.contentfabric.io/s/main/q/hq__6i1hZkSBxX15UpMmowPrLq6qbUfF4HfV88V3KQGNnGGraujxcgKCYZi16CbXxi9fpSiod5kDEN/files/Fox-All-Access-NFT-Face-V3.jpg",
              "marketplace_attributes": {
                "Eluvio": { "marketplace_id": "", "sku": "" },
                "opensea": {
                  "youtube_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9aRRiqWozaD5Gdf3UbfcpWBYjy4hRDooHNe835FRJYVKDTgSW6A1vMrdi4CbDGPoTMT5HA1XQr\u0026m=\u0026ap=\u0026lp="
                }
              },
              "media": {
                ".": {
                  "auto_update": { "tag": "latest" },
                  "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                },
                "/": "./files/Fox-All-Access-NFT-Face-V3.jpg"
              },
              "media_parameters": [],
              "media_type": "Image",
              "name": "FOX Weather All Access",
              "pack_options": {
                "hide_text": false,
                "is_openable": false,
                "item_slots": [],
                "minting_text": {
                  "minting_header": "Your pack is opening",
                  "minting_subheader1": "This may take several minutes",
                  "minting_subheader2": "You can navigate away from this page if you don't want to wait. Your items will be available in your wallet when the process is complete.",
                  "reveal_header": "Congratulations!",
                  "reveal_subheader": "You've received the following items:"
                },
                "open_animation": null,
                "open_animation_mobile": null,
                "open_button_text": "Open Pack",
                "pack_generator": "random",
                "reveal_animation": null,
                "reveal_animation_mobile": null,
                "use_custom_open_text": false
              },
              "playable": true,
              "redeemable_offers": [
                {
                  "animation": null,
                  "available_at": "2023-05-05T00:00:00-04:00",
                  "description": "\u003cp\u003eThis offer entitles you to 20% off of a Microsoft Surface at Best Buy's West LA location. Limit one per customer.\u003c/p\u003e",
                  "description_text": "",
                  "expires_at": "2025-09-05T00:00:00-04:00",
                  "image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/Best Buy Reward Square Clean.jpg"
                  },
                  "name": "20% Off Microsoft Surface West LA ",
                  "offer_id": "0",
                  "poster_image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/Best Buy Reward Poster Clean - v2.jpg"
                  },
                  "redeem_animation": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "/qfab/hq__7KThL5mWmiyw3EiYzixuhg7spLpi96gY93EY3D3w5zivnCtSUykf4L9oaA5Erj3qsgBA114L1V/meta/public/asset_metadata"
                  },
                  "redeem_animation_loop": true,
                  "require_redeem_animation": true,
                  "results_header": "",
                  "results_message": "",
                  "style": "",
                  "tags": [
                    { "key": "content", "value": "tech" },
                    { "key": "location", "value": "los angeles" }
                  ],
                  "visibility": {
                    "featured": true,
                    "hide": false,
                    "hide_if_expired": false,
                    "hide_if_unreleased": false
                  }
                },
                {
                  "animation": null,
                  "available_at": "2023-05-05T00:00:00-04:00",
                  "description": "\u003cp\u003eThis offer entitles you to one free pitcher of Prickly Pear Margaritas at The Arrogant Butcher.\u003c/p\u003e",
                  "description_text": "",
                  "expires_at": "2025-09-05T00:00:00-04:00",
                  "image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/FoxWeather_SquareAdd_Peonix_ArrogentButcherAdvert_JustImage.jpg"
                  },
                  "name": "Prickly Pear Margaritas",
                  "offer_id": "1",
                  "poster_image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/FoxWeather_VertPoster_Peonix_ArrogentButcherAdvert_JustImage.jpg"
                  },
                  "redeem_animation": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "/qfab/hq__4gRuAWP5hd3cCTrYFQG8UM2QtcznZWyzg2Bmeoq2cexMWuA7f31nbxiy5AEVHZWQhzUaXTJWCk/meta/public/asset_metadata"
                  },
                  "redeem_animation_loop": true,
                  "require_redeem_animation": true,
                  "results_header": "",
                  "results_message": "",
                  "style": "",
                  "tags": [{ "key": "location", "value": "phoenix" }],
                  "visibility": {
                    "featured": true,
                    "hide": false,
                    "hide_if_expired": false,
                    "hide_if_unreleased": false
                  }
                },
                {
                  "animation": null,
                  "available_at": "2023-05-05T00:00:00-04:00",
                  "description": "\u003cp\u003eThis offer entitles you to ten free games at Dave \u0026amp; Buster's Silver Spring location.\u003c/p\u003e",
                  "description_text": "",
                  "expires_at": "2025-09-05T00:00:00-04:00",
                  "image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/D_B Reward 1x1 Clean.jpg"
                  },
                  "name": "Free Games at Silver Spring",
                  "offer_id": "2",
                  "poster_image": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "./files/offers/D_B Reward Poster Clean.jpg"
                  },
                  "redeem_animation": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__FS8bHdmxMnUUEKQ7SVQ8P78wWM3RX4vwHPAgG6nyVVHx8o592ymKQaG6PJYmssrwavWkQq5Nut"
                    },
                    "/": "/qfab/hq__3fudRXhBvzNhXiauRUSKL4rVmUga629SUFGFxtfUrd2nmnMQKGS1acm8FSyNFuR7cqdJTxK37Q/meta/public/asset_metadata"
                  },
                  "redeem_animation_loop": true,
                  "require_redeem_animation": true,
                  "results_header": "",
                  "results_message": "",
                  "style": "",
                  "tags": [{ "key": "location", "value": "washington dc" }],
                  "visibility": {
                    "featured": true,
                    "hide": false,
                    "hide_if_expired": false,
                    "hide_if_unreleased": false
                  }
                }
              ],
              "rich_text": "",
              "secondary_resale_available_at": "",
              "secondary_resale_expires_at": "",
              "show_autoplay": false,
              "style": "",
              "subtitle": "",
              "template_id": "RV6QYnCtAWK37iiuTvW5jx",
              "terms_document": {
                "link_text": "Terms and Conditions",
                "terms_document": null
              },
              "test": false,
              "token_uri": "https://main.net955305.contentfabric.io/s/main/q/hq__9aRRiqWozaD5Gdf3UbfcpWBYjy4hRDooHNe835FRJYVKDTgSW6A1vMrdi4CbDGPoTMT5HA1XQr/meta/public/asset_metadata/nft",
              "total_supply": 10000
            }
        """.trimIndent()
        val fakeBobResponse = """
            {
              "additional_media": [
                {
                  "authorized_link": false,
                  "background_image": null,
                  "background_image_mobile": null,
                  "container": "list",
                  "controls": "Carousel",
                  "description": "",
                  "description_text": "",
                  "end_time": "",
                  "gallery": [],
                  "id": "3pmffSGd1U6u7pD6AYyzrV",
                  "image_aspect_ratio": "Square",
                  "key": "",
                  "link": "",
                  "media_file": null,
                  "media_link": null,
                  "media_reference": { "collection_id": "", "section_id": "" },
                  "media_type": "Video",
                  "name": "One Love Feature Film",
                  "offerings": [],
                  "parameters": [],
                  "requires_permissions": false,
                  "start_time": "",
                  "subtitle_1": "",
                  "subtitle_2": "",
                  "tags": []
                },
                {
                  "authorized_link": false,
                  "background_image": null,
                  "background_image_mobile": null,
                  "container": "list",
                  "controls": "Carousel",
                  "description": "",
                  "description_text": "",
                  "end_time": "",
                  "gallery": [],
                  "id": "BJDMwNmDpNZwv5YB5Mw1PF",
                  "image": "https://main.net955305.contentfabric.io/s/main/q/hq__G6bgxT4o6unWjr1iVTDvqXDNHWWuDjUaRCycCzJ1Pm84Nhtie84ku2EuMyDt9QGA1xDPWxdf9A/files/One%20Love%20Signed%20Poster%201x1.jpg",
                  "image_aspect_ratio": "Square",
                  "key": "",
                  "link": "",
                  "media_file": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                    },
                    "/": "./files/One Love Signed Poster.jpg"
                  },
                  "media_link": null,
                  "media_reference": { "collection_id": "", "section_id": "" },
                  "media_type": "Image",
                  "name": "Signed Poster",
                  "offerings": [],
                  "parameters": [],
                  "requires_permissions": false,
                  "start_time": "",
                  "subtitle_1": "",
                  "subtitle_2": "",
                  "tags": []
                },
                {
                  "authorized_link": false,
                  "background_image": null,
                  "background_image_mobile": null,
                  "container": "list",
                  "controls": "Carousel",
                  "description": "",
                  "description_text": "",
                  "end_time": "",
                  "gallery": [],
                  "id": "57iWXB1VNpXrarUyDF4Ucv",
                  "image": "https://main.net955305.contentfabric.io/s/main/q/hq__G6bgxT4o6unWjr1iVTDvqXDNHWWuDjUaRCycCzJ1Pm84Nhtie84ku2EuMyDt9QGA1xDPWxdf9A/files/One%20Love%20Bob%20Marley%20Live%20Show%201%2016x9.jpg",
                  "image_aspect_ratio": "Square",
                  "key": "",
                  "link": "",
                  "media_file": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                    },
                    "/": "./files/One Love Bob Marley Live Show 1 16x9.jpg"
                  },
                  "media_link": null,
                  "media_reference": { "collection_id": "", "section_id": "" },
                  "media_type": "Image",
                  "name": "Bob Marley Live Show 1",
                  "offerings": [],
                  "parameters": [],
                  "requires_permissions": false,
                  "start_time": "",
                  "subtitle_1": "",
                  "subtitle_2": "",
                  "tags": []
                },
                {
                  "authorized_link": false,
                  "background_image": null,
                  "background_image_mobile": null,
                  "container": "list",
                  "controls": "Carousel",
                  "description": "",
                  "description_text": "",
                  "end_time": "",
                  "gallery": [],
                  "id": "VFfyUPN9vf37iKBskJAokj",
                  "image": "https://main.net955305.contentfabric.io/s/main/q/hq__G6bgxT4o6unWjr1iVTDvqXDNHWWuDjUaRCycCzJ1Pm84Nhtie84ku2EuMyDt9QGA1xDPWxdf9A/files/One%20Love%20Bob%20Marley%20Live%20Show%202%2016x9.jpg",
                  "image_aspect_ratio": "Square",
                  "key": "",
                  "link": "",
                  "media_file": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                    },
                    "/": "./files/One Love Bob Marley Live Show 2 16x9.jpg"
                  },
                  "media_link": null,
                  "media_reference": { "collection_id": "", "section_id": "" },
                  "media_type": "Image",
                  "name": "Bob Marley Live Show 2",
                  "offerings": [],
                  "parameters": [],
                  "requires_permissions": false,
                  "start_time": "",
                  "subtitle_1": "",
                  "subtitle_2": "",
                  "tags": []
                },
                {
                  "authorized_link": false,
                  "background_image": null,
                  "background_image_mobile": null,
                  "container": "list",
                  "controls": "Carousel",
                  "description": "",
                  "description_text": "",
                  "end_time": "",
                  "gallery": [],
                  "id": "AQJnpwsiMhMhzMhmxVStAM",
                  "image": "https://main.net955305.contentfabric.io/s/main/q/hq__G6bgxT4o6unWjr1iVTDvqXDNHWWuDjUaRCycCzJ1Pm84Nhtie84ku2EuMyDt9QGA1xDPWxdf9A/files/One%20Love%20Bob%20Marley%20Live%20Show%203%2016x9.jpg",
                  "image_aspect_ratio": "Square",
                  "key": "",
                  "link": "",
                  "media_file": {
                    ".": {
                      "auto_update": { "tag": "latest" },
                      "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                    },
                    "/": "./files/One Love Bob Marley Live Show 3 16x9.jpg"
                  },
                  "media_link": null,
                  "media_reference": { "collection_id": "", "section_id": "" },
                  "media_type": "Image",
                  "name": "Bob Marley Live Show 3",
                  "offerings": [],
                  "parameters": [],
                  "requires_permissions": false,
                  "start_time": "",
                  "subtitle_1": "",
                  "subtitle_2": "",
                  "tags": []
                }
              ],
              "additional_media_custom_css": "",
              "additional_media_display": "Media",
              "additional_media_sections": {
                "featured_media": [
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Logo.png"
                    },
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love BG.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "One Love takes us through the life of Marley from his upbringing in Jamaica all the way to when he became the most famous reggae singer in the world. We get to witness moments from his private life, such as scenes with his family at home and playing soccer together.",
                    "end_time": "",
                    "gallery": [],
                    "id": "AhutXu4Rv92v1ooWNRZCi9",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__CzFwBzww2uKin5VP2udgBkr2Bpo9yiAN1TkyrJdQDQzN7VY1vDACV3QB7VfNW8V78oGfWeQ1be/files/One%20Love%20Feature%20Film%201x1.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__CzFwBzww2uKin5VP2udgBkr2Bpo9yiAN1TkyrJdQDQzN7VY1vDACV3QB7VfNW8V78oGfWeQ1be/files/One%20Love%20Feature%20Film%201x1.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": false,
                      "lock_condition": "View Media",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "locked_state": {
                      "animation": null,
                      "background_image": null,
                      "button_image": null,
                      "button_text": "",
                      "description": "",
                      "description_text": "",
                      "name": "",
                      "subtitle_1": "",
                      "subtitle_2": ""
                    },
                    "media_file": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love Feature Film 16x9.jpg"
                    },
                    "media_link": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "/qfab/hq__3qChzMEkpzsJtde65yxekhnHZitGe43jBAz58PdU4e56KVxKUbPqQFYuvoPu2jCq3CDPJoDHRV/meta/public/asset_metadata"
                    },
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Video",
                    "name": "Bob Marley One Love Feature Film",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love Poster.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": [
                      { "key": "Director", "value": "Reinaldo Marcus Green" },
                      {
                        "key": "Writers",
                        "value": "Terence Winter, Frank E. Flowers, Zach Baylin"
                      },
                      {
                        "key": "Stars",
                        "value": "Kingsley Ben-Adir, Micheal Ward, James Norton"
                      },
                      { "key": "Language", "value": "English" },
                      { "key": "Rating", "value": "TV-PG" },
                      { "key": "Release Date", "value": "2024" },
                      {
                        "key": "Style",
                        "value": "Biographical, Drama, Musical, Film"
                      }
                    ]
                  },
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Logo.png"
                    },
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love BG.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "43ykFfvm6RLnWwTcxFfJCq",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Tickets%201x1.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Tickets%201x1.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": false,
                      "lock_condition": "View Media",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "locked_state": {
                      "animation": null,
                      "background_image": null,
                      "button_image": null,
                      "button_text": "",
                      "description": "",
                      "description_text": "",
                      "name": "",
                      "subtitle_1": "",
                      "subtitle_2": ""
                    },
                    "media_file": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Tickets - Poster Version.png"
                    },
                    "media_link": null,
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Image",
                    "name": "Two Movie Tickets",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Tickets - Poster Version.png"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": []
                  },
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Logo.png"
                    },
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love BG.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "HBKvBR6WYGpsTYdhHjVPBx",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Signed%20Poster%201x1.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Signed%20Poster%201x1.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": false,
                      "lock_condition": "View Media",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "locked_state": {
                      "animation": null,
                      "background_image": null,
                      "button_image": null,
                      "button_text": "",
                      "description": "",
                      "description_text": "",
                      "name": "",
                      "subtitle_1": "",
                      "subtitle_2": ""
                    },
                    "media_file": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Signed Poster - Full Sized.jpg"
                    },
                    "media_link": null,
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Image",
                    "name": "Signed Poster",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Signed Poster - Poster Version.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": []
                  },
                  {
                    "animation": null,
                    "authorized_link": false,
                    "background_image": null,
                    "background_image_logo_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Logo.png"
                    },
                    "background_image_mobile": null,
                    "background_image_tv": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/One Love BG.jpg"
                    },
                    "button_image": null,
                    "button_text": "",
                    "container": "featured",
                    "controls": "Carousel",
                    "description": "",
                    "description_text": "",
                    "end_time": "",
                    "gallery": [],
                    "id": "6wyThYzdVvhmoFRNaPjvY6",
                    "image": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Signed%20Script%201x1.jpg",
                    "image_aspect_ratio": "Square",
                    "image_tv": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/files/2023-09-29/One%20Love%20Signed%20Script%201x1.jpg",
                    "key": "",
                    "link": "",
                    "lock_conditions": {
                      "hide_when_locked": false,
                      "lock_condition": "View Media",
                      "required_attributes": [],
                      "required_media": []
                    },
                    "locked": false,
                    "locked_state": {
                      "animation": null,
                      "background_image": null,
                      "button_image": null,
                      "button_text": "",
                      "description": "",
                      "description_text": "",
                      "name": "",
                      "subtitle_1": "",
                      "subtitle_2": ""
                    },
                    "media_file": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Signed Script - Poster Version.jpg"
                    },
                    "media_link": null,
                    "media_reference": { "collection_id": "", "section_id": "" },
                    "media_type": "Image",
                    "name": "Signed Script",
                    "offerings": [],
                    "parameters": [],
                    "poster_image": {
                      ".": {
                        "auto_update": { "tag": "latest" },
                        "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                      },
                      "/": "./files/2023-09-29/One Love Signed Script - Poster Version.jpg"
                    },
                    "required": false,
                    "requires_permissions": false,
                    "start_time": "",
                    "subtitle_1": "",
                    "subtitle_2": "",
                    "tags": []
                  }
                ],
                "sections": [
                  {
                    "collections": [
                      {
                        "display": "Media",
                        "id": "7hNSeNchs7Dqh8gg2cpJ7f",
                        "media": [
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "R5nkE4sadNdUnpa6fAVnT2",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__CzFwBzww2uKin5VP2udgBkr2Bpo9yiAN1TkyrJdQDQzN7VY1vDACV3QB7VfNW8V78oGfWeQ1be/files/One%20Love%20Bob%20Marley%20Live%20Show%201%2016x9.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "locked_state": {
                              "description": "",
                              "description_text": "",
                              "hide_when_locked": false,
                              "image_aspect_ratio": "Square",
                              "lock_condition": "View Media",
                              "name": "",
                              "required_attributes": [],
                              "required_media": [],
                              "subtitle_1": "",
                              "subtitle_2": ""
                            },
                            "media_file": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                              },
                              "/": "./files/One Love Bob Marley Live Show 1 16x9.jpg"
                            },
                            "media_link": null,
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Image",
                            "name": "Live Show 1",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": []
                          },
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "4BxrhuHdWwyHdg33S2XGe5",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__CzFwBzww2uKin5VP2udgBkr2Bpo9yiAN1TkyrJdQDQzN7VY1vDACV3QB7VfNW8V78oGfWeQ1be/files/One%20Love%20Bob%20Marley%20Live%20Show%202%2016x9.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "locked_state": {
                              "description": "",
                              "description_text": "",
                              "hide_when_locked": false,
                              "image_aspect_ratio": "Square",
                              "lock_condition": "View Media",
                              "name": "",
                              "required_attributes": [],
                              "required_media": [],
                              "subtitle_1": "",
                              "subtitle_2": ""
                            },
                            "media_file": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                              },
                              "/": "./files/One Love Bob Marley Live Show 2 16x9.jpg"
                            },
                            "media_link": null,
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Image",
                            "name": "Live Show 2",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": []
                          },
                          {
                            "authorized_link": false,
                            "background_image": null,
                            "background_image_mobile": null,
                            "container": "collection",
                            "controls": "Carousel",
                            "description": "",
                            "description_text": "",
                            "end_time": "",
                            "gallery": [],
                            "id": "EmChBT8ZhDPkiyuZEB9Tjs",
                            "image": "https://main.net955305.contentfabric.io/s/main/q/hq__CzFwBzww2uKin5VP2udgBkr2Bpo9yiAN1TkyrJdQDQzN7VY1vDACV3QB7VfNW8V78oGfWeQ1be/files/One%20Love%20Bob%20Marley%20Live%20Show%203%2016x9.jpg",
                            "image_aspect_ratio": "Wide",
                            "key": "",
                            "link": "",
                            "locked": false,
                            "locked_state": {
                              "description": "",
                              "description_text": "",
                              "hide_when_locked": false,
                              "image_aspect_ratio": "Square",
                              "lock_condition": "View Media",
                              "name": "",
                              "required_attributes": [],
                              "required_media": [],
                              "subtitle_1": "",
                              "subtitle_2": ""
                            },
                            "media_file": {
                              ".": {
                                "auto_update": { "tag": "latest" },
                                "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                              },
                              "/": "./files/One Love Bob Marley Live Show 3 16x9.jpg"
                            },
                            "media_link": null,
                            "media_reference": {
                              "collection_id": "",
                              "section_id": ""
                            },
                            "media_type": "Image",
                            "name": "Live Show 3",
                            "offerings": [],
                            "parameters": [],
                            "requires_permissions": false,
                            "start_time": "",
                            "subtitle_1": "",
                            "subtitle_2": "",
                            "tags": []
                          }
                        ],
                        "name": "",
                        "show_autoplay": false
                      }
                    ],
                    "id": "LfgFnb9fXUbRFnMDiix7b1",
                    "name": "Live Performances From Archive"
                  }
                ]
              },
              "additional_media_type": "Sections",
              "address": "0xfe6eca032ff865731c280d1f10f641a573b3ffb6",
              "attributes": [],
              "background_image": {
                ".": {
                  "auto_update": { "tag": "latest" },
                  "container": "hq__EFwiY1iixerPGVqn2rRHqcLtb6KZVtrdbJNGiSd6tWf2ssacL9iJokoihc2ZmkT5Gj7upBiRK6"
                },
                "/": "./files/One Love BG.jpg"
              },
              "copyright": "",
              "created_at": "",
              "creator": "",
              "description": "This bundle includes access to the Bob Marley One Love Feature Film before it is released in theaters, as well as a special signed poster and exclusive live performance recordings.",
              "description_rich_text": "",
              "display_name": "Super Movie Bundle: Bob Marley One Love",
              "edition_name": "Genesis Edition",
              "embed_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8\u0026m=\u0026ap=\u0026lp=",
              "external_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8\u0026m=\u0026ap=\u0026lp=",
              "generative": false,
              "has_audio": false,
              "id_format": "token_id",
              "image": "https://main.net955305.contentfabric.io/s/main/q/hq__2zzgyVzrfmwVwL7KdtSk7z1Rine86F54RQ8ZWxw13jrzs9zPUP8ywuo7iWo7EJp7CRsZgWgTui/files/One%20Love%20NFT%201x1%201.jpg",
              "marketplace_attributes": {
                "Eluvio": { "marketplace_id": "", "sku": "" },
                "opensea": {
                  "youtube_url": "https://embed.v3.contentfabric.io/?p=\u0026net=main\u0026vid=hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8\u0026m=\u0026ap=\u0026lp="
                }
              },
              "media": null,
              "media_parameters": [],
              "media_type": "Image",
              "name": "Super Movie Bundle: Bob Marley One Love",
              "pack_options": {
                "hide_text": false,
                "is_openable": false,
                "item_slots": [],
                "minting_text": {
                  "minting_header": "Your pack is opening",
                  "minting_subheader1": "This may take several minutes",
                  "minting_subheader2": "You can navigate away from this page if you don't want to wait. Your items will be available in your wallet when the process is complete.",
                  "reveal_header": "Congratulations!",
                  "reveal_subheader": "You've received the following items:"
                },
                "open_animation": null,
                "open_animation_mobile": null,
                "open_button_text": "Open Pack",
                "pack_generator": "random",
                "reveal_animation": null,
                "reveal_animation_mobile": null,
                "use_custom_open_text": false
              },
              "playable": false,
              "redeemable_offers": [],
              "rich_text": "",
              "secondary_resale_available_at": "",
              "secondary_resale_expires_at": "",
              "show_autoplay": false,
              "style": "",
              "subtitle": "",
              "template_id": "GxQyHjzDgeG5vS7FPmJ6B1",
              "terms_document": {
                "link_text": "Terms and Conditions",
                "terms_document": null
              },
              "test": false,
              "token_uri": "https://main.net955305.contentfabric.io/s/main/q/hq__9ru5C8d8CLeMzFYuT4HUcBfwaijGyE9MFrkT2PKr1fiLxcr3uT9YDBDKyKDTz2kp2jMLEQ3mA8/meta/public/asset_metadata/nft",
              "total_supply": 1000
            }
        """.trimIndent()
    }
}
