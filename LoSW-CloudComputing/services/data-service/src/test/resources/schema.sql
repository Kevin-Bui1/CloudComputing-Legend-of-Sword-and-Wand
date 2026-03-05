-- 1. USERS TABLE
-- Handles profile creation and authentication as required by the use cases.
CREATE TABLE Users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL, -- In a real app, store hashed passwords
                       pvp_wins INT DEFAULT 0,         -- To track league stats for PvP 
                       pvp_losses INT DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PARTIES TABLE
-- Stores saved parties and campaign progress.
-- A user can have up to 5 saved parties.
CREATE TABLE Parties (
                         party_id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT NOT NULL,
                         party_name VARCHAR(50),
                         is_active_campaign BOOLEAN DEFAULT TRUE, -- Distinguishes between a current run and a saved PvP team
                         current_room INT DEFAULT 1,              -- Tracks progress up to room 30 
                         gold INT DEFAULT 0,                      -- Stores gold for purchasing items 
                         score INT DEFAULT 0,                     -- Calculated score at the end of a campaign 
                         FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 3. HEROES TABLE
-- Stores individual hero stats. A party has 1 to 5 members.
CREATE TABLE Heroes (
                        hero_id INT AUTO_INCREMENT PRIMARY KEY,
                        party_id INT NOT NULL,
                        hero_name VARCHAR(50),
                        hero_class VARCHAR(20),       -- Order, Chaos, Warrior, Mage 
                        level INT DEFAULT 1,          -- Heroes start at level 1 and go up to 20 
                        experience INT DEFAULT 0,     -- Experience points 
                        hp_current INT DEFAULT 100,   -- Default starting HP 
                        hp_max INT DEFAULT 100,
                        mana_current INT DEFAULT 50,  -- Default starting Mana 
                        mana_max INT DEFAULT 50,
                        attack INT DEFAULT 5,         -- Default starting Attack 
                        defense INT DEFAULT 5,        -- Default starting Defense 
                        FOREIGN KEY (party_id) REFERENCES Parties(party_id) ON DELETE CASCADE
);

-- 4. INVENTORY TABLE
-- Stores items owned by the party (e.g., Bread, Elixir).
CREATE TABLE Inventory (
                           inventory_id INT AUTO_INCREMENT PRIMARY KEY,
                           party_id INT NOT NULL,
                           item_name VARCHAR(50),        -- 'Bread', 'Cheese', 'Steak', 'Water', 'Juice', 'Wine', 'Elixir'
                           quantity INT DEFAULT 0,
                           FOREIGN KEY (party_id) REFERENCES Parties(party_id) ON DELETE CASCADE
);

-- 5. HIGH SCORES TABLE (HALL OF FAME)
-- Keeps a record of the highest scores among players.
CREATE TABLE HighScores (
                            score_id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            score INT NOT NULL,
                            date_achieved DATE,
                            FOREIGN KEY (user_id) REFERENCES Users(user_id)
);