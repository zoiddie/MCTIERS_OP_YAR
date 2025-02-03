
import requests

def get_player_vanilla_tier(player_name):
    search_url = f"https://mctiers.com/api/search_profile/{player_name}"
    response = requests.get(search_url)
    if response.status_code != 200:
        print(f"Error searching for player: {response.status_code}")
        return None
    player_data = response.json()
    if not player_data or not player_data.get("uuid"):
        print(f"Player {player_name} not found.")
        return None

    if "vanilla" in player_data["rankings"]:
        tier = player_data["rankings"]["vanilla"]["tier"]
        tier_type = "HT" if tier <= 2 else "LT"
        return f"{tier_type}{tier}"
    else:
        return "Player not found in vanilla rankings."

player_name = int(input("Enter the player name: "))
player_tier = get_player_vanilla_tier(player_name)
print(f"The tier of {player_name} in vanilla is: {player_tier}")

