import requests
from tabulate import tabulate
from colorama import Fore, Style, init

init(autoreset=True)

def beautify_floor_plans(floor_plans):
    formatted_floor_plans = []
    if not floor_plans:
        return "No floor plan data found."
        
    for floor in floor_plans:
        formatted_floor_plan = [f"{Fore.CYAN}Floor {floor['floorNumber']}{Style.RESET_ALL}"]
        formatted_rooms = []
        for room in floor.get('rooms', []):
            capacity_color = get_capacity_color(room.get('capacity', 0))
            room_id = room.get('roomId', 'N/A')
            is_available = room.get('isAvailable')
            if is_available is True:
                is_available_str = f"{Fore.GREEN}Yes{Style.RESET_ALL}"
            else:
                is_available_str = f"{Fore.RED}No{Style.RESET_ALL}"
            
            formatted_rooms.append(
                f"{capacity_color}| Room ID: {room_id}\n"   
                f"|   Name: {room.get('name', 'Unnamed')}\n"
                f"|   Capacity: {room.get('capacity', 0)}\n"
                f"|   Available: {is_available_str}{Style.RESET_ALL}"
            )
        formatted_floor_plan.extend(formatted_rooms)
        formatted_floor_plans.append(formatted_floor_plan)
        
    return tabulate(formatted_floor_plans, tablefmt='fancy_grid')

def get_capacity_color(capacity):
    if capacity > 20:
        return Fore.RED
    elif capacity > 10:
        return Fore.YELLOW
    else:
        return Fore.GREEN

def colorful_input(prompt):
    return input(f"{Fore.BLUE}{Style.BRIGHT}{prompt}{Style.RESET_ALL}")

def main():
    api_url = "http://localhost:8080/api/floorplan" 
    
    print("Welcome to the Floor Plan Manager")
    while True:
        print("\n" + "="*30)
        username = colorful_input("Enter username: ")
        version = colorful_input("Enter version tag (e.g., v1, v2): ")

        print("\n--- Operations ---")
        print("1. View Latest Floor Plan")
        print("2. Save New Floor Plan Version (Admin only)")
        print("3. Recommend Rooms (from this version)")
        print("4. Book a Room (in this version)")
        print("0. Exit")
        operation = colorful_input("Select operation (1, 2, 3, 4, or 0): ")

        if operation == '0':
            print("Exiting.")
            break

        if operation == '1':
            # view latest state of the floor plan for the given version tag
            params = {"username": username, "version": version}
            response = requests.get(api_url, params=params)
            
            if response.status_code == 200:
                try:
                    floor_plans = response.json()
                    print(f"\n{Fore.GREEN}Latest state for version: {version}{Style.RESET_ALL}")
                    print(beautify_floor_plans(floor_plans))
                except requests.exceptions.JSONDecodeError:
                    print(f"{Fore.RED}Error: Invalid response from server.{Style.RESET_ALL}")
            else:
                print(f"{Fore.RED}Failed (Code: {response.status_code}): {response.text}{Style.RESET_ALL}")

        elif operation == '2':
            # save a new floor plan design
            if username != 'admin':
                print(f"{Fore.RED}Only 'admin' can save a new floor plan.{Style.RESET_ALL}")
                continue
            
            num_floors = int(colorful_input("Enter number of floors: "))
            updated_floor_plans = []
            for i in range(1, num_floors + 1):
                num_rooms = int(colorful_input(f"Enter number of rooms for Floor {i}: "))
                rooms = []
                for j in range(1, num_rooms + 1):
                    room_name = colorful_input(f"Enter name for Room {j} on Floor {i}: ")
                    room_capacity = int(colorful_input(f"Enter capacity for Room {j} on Floor {i}: "))
                    rooms.append({"name": room_name, "capacity": room_capacity})
                updated_floor_plans.append({"floorNumber": i, "rooms": rooms})
            
            payload = {"username": username, "version": version, "floorDTOs": updated_floor_plans}
            response = requests.post(api_url + '/update', json=payload)
            print(f"\n{Fore.GREEN}{response.text}{Style.RESET_ALL}")

        elif operation == '3':
            # recommend rooms using the versionTag
            participants = int(colorful_input("Enter number of participants: "))
            last_room_name = colorful_input("Enter last booked room name (or press Enter): ")

            params = {
                "versionTag": version,
                "participants": participants,
                "lastRoomName": last_room_name
            }
            response = requests.get(f"{api_url}/recommend-rooms", params=params)
            
            if response.status_code == 200:
                recommended_rooms = response.json()
                if recommended_rooms:
                    print(f"\n{Fore.GREEN}Recommended Rooms from version '{version}':{Style.RESET_ALL}")
                    for i, room in enumerate(recommended_rooms, 1):
                        print(f"{i}. {room['name']} (Capacity: {room['capacity']})")
                else:
                    print(f"{Fore.YELLOW}No rooms available for these criteria in version '{version}'.{Style.RESET_ALL}")
            else:
                print(f"{Fore.RED}Failed (Code: {response.status_code}): {response.text}{Style.RESET_ALL}")

        elif operation == '4':
            # book a room, which will create a new history entry
            room_name = colorful_input("Enter the Room ID to book: ")
            participants = int(colorful_input("Enter number of participants: "))

            booking_payload = {"roomName": room_name, "participants": participants, "version": version}
            response = requests.post(f"{api_url}/book-room", json=booking_payload)
            
            if response.status_code == 200:
                print(f"\n{Fore.GREEN}{response.text}{Style.RESET_ALL}")
            else:
                print(f"\n{Fore.RED}Booking failed (Code: {response.status_code}): {response.text}{Style.RESET_ALL}")

        else:
            print(f"{Fore.RED}Invalid operation selected.{Style.RESET_ALL}")

if __name__ == "__main__":
    main()