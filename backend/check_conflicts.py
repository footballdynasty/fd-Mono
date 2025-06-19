#!/usr/bin/env python3

import requests
import json
from collections import defaultdict

def check_team_conflicts():
    """Check if any team has multiple games in the same week"""
    base_url = "http://localhost:8080/api/v2"
    
    # Get all weeks 1-15 and check for conflicts
    for week_num in range(1, 16):
        print(f"\nChecking Week {week_num}...")
        
        # Get games for this week
        response = requests.get(f"{base_url}/games", params={
            "year": 2025,
            "week": week_num,
            "size": 1000  # Get all games for the week
        })
        
        if response.status_code != 200:
            print(f"  Error getting games for week {week_num}: {response.status_code}")
            continue
            
        data = response.json()
        games = data.get('content', [])
        
        if not games:
            print(f"  No games found for week {week_num}")
            continue
            
        # Track teams in this week
        teams_in_week = defaultdict(list)
        
        for game in games:
            home_team = game['homeTeamName']
            away_team = game['awayTeamName']
            game_id = game['gameId']
            
            teams_in_week[home_team].append(f"Home in {game_id}")
            teams_in_week[away_team].append(f"Away in {game_id}")
        
        # Check for conflicts
        conflicts_found = False
        for team, games_list in teams_in_week.items():
            if len(games_list) > 1:
                print(f"  CONFLICT: {team} has {len(games_list)} games:")
                for game_info in games_list:
                    print(f"    - {game_info}")
                conflicts_found = True
        
        if not conflicts_found:
            print(f"  Week {week_num}: ✓ No conflicts ({len(games)} games, {len(teams_in_week)} teams)")

if __name__ == "__main__":
    try:
        check_team_conflicts()
        print("\n✓ Conflict check completed!")
    except Exception as e:
        print(f"Error: {e}")