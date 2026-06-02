import { useState, useEffect } from 'react';
import './App.css';

// Premium background images mapped to cuisines
const CUISINE_IMAGES = {
  italian: "https://lh3.googleusercontent.com/aida-public/AB6AXuA1-kTuBu8KEbfE6BxzaR0ab86Ogfg6wlOcZIiT_YkIIlKmQbveL6-i8i_2OIbfDekhZFmTrDG0D5xt0PqXh8rcNus8e3g0Tu78Gz7QJsmN7cY8ad-Etd4IVPkvJfz0-R0a4LOcJmzD2-rWBF7xPhGAIBMC-iZDtRyr98NJ5WZnlbJOIs1JdJ_LII1DNx9m0Q7qkbZRg3CnyZaLl1h_9W4r4MuDotCI9XpITTY72eh1S8rlST2SSnYtehXYPYnGnJwMbwOLemoB2K7T",
  pizza: "https://lh3.googleusercontent.com/aida-public/AB6AXuCWfzhZOD9v5caMc7hs3kfzjJ9N_umOIfZ4blPyQ4qMQ3AoR8Bb01-zRww3ROz0rtb_qQjOEou_jJAgnvgmq2BtHZnV9plz1SyXTNt9Qr6tZht_f2H4_1wIqHNHKUu2iFgfhjC2N9T0pZmzBpooCRWmz23MHPREnLh-qv8mKc2E5_YuZjD7a9ocZNDfDuKjqmx9NcTMqedsBs0Jn1OdT5Rl09I3NU9jqUAYTonSQjptf7GlBdqfyeSohC8bEn1ef__jH5W90oz8wrn6",
  continental: "https://lh3.googleusercontent.com/aida-public/AB6AXuDEVcOestnhR0w43w0qXif_sx8fmCR5GVYmKVaQvSl2swXXMDZ_76XnSgDvz4q_E8szOLyi9qwKj0ZNYtqtQF5wUpcllsPrs_TetvdbT23XOjY_E7v5MZ0XJuGTI_8kFwLvC4y15eU5_qjm2sXCJay31-Z4aaKY6exTG5COeMbSvuqIbmhn83kmS_bLXa0Yh9OAbQe0ZaeF0LYdM29oXqjCNrJ4psxB0yq-wWb2kIcTilGedzqBN7rPCN79o4fpPKltMSzh6tv0e9ID",
  chinese: "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&auto=format&fit=crop&q=80",
  indian: "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=800&auto=format&fit=crop&q=80",
  cafe: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&auto=format&fit=crop&q=80",
  burger: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=80",
  fallback: "https://images.unsplash.com/photo-1544025162-d76694265947?w=800&auto=format&fit=crop&q=80"
};

function getCuisineImage(cuisinesList) {
  if (!cuisinesList || cuisinesList.length === 0) return CUISINE_IMAGES.fallback;
  const primary = cuisinesList[0].toLowerCase();
  
  if (primary.includes('pizza')) return CUISINE_IMAGES.pizza;
  if (primary.includes('italian')) return CUISINE_IMAGES.italian;
  if (primary.includes('chinese') || primary.includes('thai') || primary.includes('asian')) return CUISINE_IMAGES.chinese;
  if (primary.includes('indian')) return CUISINE_IMAGES.indian;
  if (primary.includes('continental')) return CUISINE_IMAGES.continental;
  if (primary.includes('cafe')) return CUISINE_IMAGES.cafe;
  if (primary.includes('burger') || primary.includes('fast food')) return CUISINE_IMAGES.burger;
  
  return CUISINE_IMAGES.fallback;
}

export default function App() {
  const [view, setView] = useState('search'); // 'search' | 'loading' | 'results'
  const [cities, setCities] = useState(['Bangalore', 'Delhi']);
  const [cuisines, setCuisines] = useState(['Any Cuisine', 'Italian', 'Chinese', 'North Indian']);
  
  // Form states
  const [selectedCity, setSelectedCity] = useState('Bangalore');
  const [selectedCuisine, setSelectedCuisine] = useState('Any Cuisine');
  const [budget, setBudget] = useState('MEDIUM'); // 'LOW' | 'MEDIUM' | 'HIGH'
  const [minRating, setMinRating] = useState(4.0);
  const [additionalPreferences, setAdditionalPreferences] = useState('');
  
  // API response states
  const [recommendations, setRecommendations] = useState([]);
  const [summary, setSummary] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [isDemoMode, setIsDemoMode] = useState(false);

  // Load distinct cities on mount
  useEffect(() => {
    fetch('/api/v1/metadata/cities')
      .then(res => {
        if (!res.ok) throw new Error('API failure');
        return res.json();
      })
      .then(data => {
        if (data && data.length > 0) {
          setCities(data);
          setSelectedCity(data[0]);
        }
      })
      .catch(() => {
        // Silent fallback to standard defaults
      });
  }, []);

  // Fetch cuisines when city changes
  useEffect(() => {
    const url = selectedCity 
      ? `/api/v1/metadata/cuisines?city=${encodeURIComponent(selectedCity)}` 
      : '/api/v1/metadata/cuisines';
      
    fetch(url)
      .then(res => {
        if (!res.ok) throw new Error('API failure');
        return res.json();
      })
      .then(data => {
        if (data && data.length > 0) {
          setCuisines(['Any Cuisine', ...data]);
        }
      })
      .catch(() => {
        // Fallback default cuisines
        setCuisines(['Any Cuisine', 'Italian', 'Chinese', 'North Indian', 'Continental', 'Cafe']);
      });
  }, [selectedCity]);

  // Submit preferences to the API
  const handleCurate = async () => {
    setView('loading');
    setIsDemoMode(false);
    
    // Minimum 1.5 seconds loading state to show the premium thinking animation
    const minLoadingTime = new Promise(resolve => setTimeout(resolve, 1800));
    
    const requestPayload = {
      location: selectedCity,
      budget: budget,
      cuisine: selectedCuisine === 'Any Cuisine' ? 'Any' : selectedCuisine,
      minRating: parseFloat(minRating),
      additionalPreferences: additionalPreferences || "Curated recommendation",
      topN: 3
    };

    try {
      const responsePromise = fetch('/api/v1/recommendations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestPayload)
      }).then(res => {
        if (!res.ok) throw new Error('Server returned error status');
        return res.json();
      });

      // Wait for both the minimum loading animation and the API response
      const [apiData] = await Promise.all([responsePromise, minLoadingTime]);
      
      setRecommendations(apiData.recommendations || []);
      setSummary(apiData.summary || 'AI has curated these premium selections based on your preferences.');
      setSuggestions(apiData.suggestions || []);
      setView('results');
    } catch (err) {
      console.warn("Backend API not reachable. Switching to premium demo simulation mode...", err);
      // Let the loader animate for a moment longer
      await minLoadingTime;
      
      // Load curated fallback mock data matching user criteria
      setIsDemoMode(true);
      generateMockRecommendations(requestPayload);
      setView('results');
    }
  };

  // Curated premium mock data generator for offline / fallback demonstration
  const generateMockRecommendations = (payload) => {
    const isItalian = payload.cuisine.toLowerCase().includes('italian') || payload.cuisine.toLowerCase().includes('any');
    const isChinese = payload.cuisine.toLowerCase().includes('chinese');
    const isIndian = payload.cuisine.toLowerCase().includes('indian') || payload.cuisine.toLowerCase().includes('north');

    let mockRecs = [];
    if (isItalian) {
      mockRecs = [
        {
          restaurantId: "1",
          name: "The Italian Bistro",
          cuisines: ["Italian", "Romantic", "Premium"],
          rating: 4.8,
          costForTwo: 1800,
          explanation: "Matches your Italian preference perfectly. Known for its quiet, intimate ambiance which aligns seamlessly with a date-night setting."
        },
        {
          restaurantId: "2",
          name: "Napoli Woodfire",
          cuisines: ["Pizza", "Cozy", "Italian"],
          rating: 4.5,
          costForTwo: 1200,
          explanation: "Highly rated for authentic woodfired pizzas. Slightly livelier vibe but excellent value for two."
        },
        {
          restaurantId: "3",
          name: "The Glasshouse",
          cuisines: ["Continental", "Fine Dining", "Elegant"],
          rating: 4.6,
          costForTwo: 2200,
          explanation: "A great alternative to Italian. Offers a stunning view and a very quiet atmosphere. Slightly above budget."
        }
      ];
    } else if (isChinese) {
      mockRecs = [
        {
          restaurantId: "4",
          name: "Dragon Wok",
          cuisines: ["Chinese", "Thai", "Asian"],
          rating: 4.6,
          costForTwo: 1200,
          explanation: "Premium modern Asian dining. Matches your preference for Chinese, presenting high culinary precision in a vibrant, sleek environment."
        },
        {
          restaurantId: "5",
          name: "Dim Sum House",
          cuisines: ["Dim Sum", "Cozy", "Authentic"],
          rating: 4.4,
          costForTwo: 900,
          explanation: "Fantastic handcrafted dumplings. Excellent casual cozy setting suitable for relaxing conversations."
        }
      ];
    } else if (isIndian) {
      mockRecs = [
        {
          restaurantId: "6",
          name: "Spice Garden",
          cuisines: ["North Indian", "Mughlai", "Traditional"],
          rating: 4.5,
          costForTwo: 800,
          explanation: "Renowned for its traditional copper bowl curry preparations and warm hospitality. Very balanced spice profiles."
        },
        {
          restaurantId: "7",
          name: "The Royal Feast",
          cuisines: ["Indian", "Fine Dining", "Luxury"],
          rating: 4.7,
          costForTwo: 2500,
          explanation: "High-end Indian dining experience. Perfect service with signature royal entrees and private booths."
        }
      ];
    } else {
      mockRecs = [
        {
          restaurantId: "8",
          name: `The ${payload.cuisine} Garden`,
          cuisines: [payload.cuisine, "Gourmet", "Fresh"],
          rating: 4.7,
          costForTwo: 1500,
          explanation: `Tailored precisely to your ${payload.cuisine} selection in ${payload.location}. Boasts high user satisfaction and matching characteristics.`
        }
      ];
    }

    setRecommendations(mockRecs);
    setSummary(`Here are the top personalized restaurant recommendations selected in ${payload.location}.`);
  };

  const handleQuickTagClick = (tag) => {
    setAdditionalPreferences(tag);
  };

  return (
    <>
      {/* Top Navigation Bar */}
      <header className="fixed top-0 left-0 w-full flex justify-between items-center px-8 md:px-16 py-4 bg-surface/80 backdrop-blur-xl dark:bg-surface-dim/80 border-b border-white/10 shadow-lg z-50">
        <div 
          onClick={() => setView('search')} 
          className="flex items-center gap-2 hover:opacity-80 transition-opacity cursor-pointer"
        >
          <span className="material-symbols-outlined text-primary dark:text-primary-fixed-dim text-3xl" style={{ fontVariationSettings: "'FILL' 1" }}>menu_book</span>
          <span className="font-headline-md text-2xl font-extrabold text-primary dark:text-primary-fixed-dim tracking-tight">Zomato AI</span>
        </div>
        
        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-8">
          <button 
            onClick={() => setView('search')}
            className={`flex items-center gap-2 transition-opacity hover:opacity-80 ${view === 'search' ? 'text-primary dark:text-primary-fixed-dim' : 'text-secondary dark:text-secondary-fixed-dim opacity-70'}`}
          >
            <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 1" }}>explore</span>
            <span className="font-label-md text-label-md">Discover</span>
          </button>
          <button 
            onClick={() => {
              if (recommendations.length > 0) setView('results');
            }}
            disabled={recommendations.length === 0}
            className={`flex items-center gap-2 transition-opacity hover:opacity-80 disabled:cursor-not-allowed ${view === 'results' ? 'text-primary dark:text-primary-fixed-dim' : 'text-secondary dark:text-secondary-fixed-dim opacity-70'} ${recommendations.length === 0 ? 'opacity-30' : ''}`}
          >
            <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: view === 'results' ? "'FILL' 1" : "'FILL' 0" }}>smart_toy</span>
            <span className="font-label-md text-label-md">AI Chat</span>
          </button>
          <button className="flex items-center gap-2 text-secondary dark:text-secondary-fixed-dim opacity-70 hover:text-primary transition-colors">
            <span className="material-symbols-outlined text-xl">bookmark</span>
            <span className="font-label-md text-label-md">Saved</span>
          </button>
        </nav>

        <div className="flex items-center hover:opacity-80 transition-opacity cursor-pointer">
          <div className="w-10 h-10 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface-variant overflow-hidden border border-outline-variant shadow-sm">
            <span className="material-symbols-outlined text-lg">person</span>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-grow pt-[100px] px-8 md:px-16 flex flex-col items-center justify-center min-h-screen relative overflow-hidden w-full">
        
        {/* Ambient Decorative Background Glows */}
        <div className="absolute inset-0 z-0 pointer-events-none overflow-hidden flex items-center justify-center opacity-40">
          <div className="w-[1000px] h-[1000px] rounded-full bg-gradient-to-tr from-primary/10 to-transparent blur-[120px] absolute -top-[300px] -left-[200px]"></div>
          <div className="w-[800px] h-[800px] rounded-full bg-gradient-to-bl from-tertiary/10 to-transparent blur-[100px] absolute bottom-[-100px] right-[-100px]"></div>
        </div>

        {/* VIEW 1: HERO SEARCH CARD */}
        {view === 'search' && (
          <div className="w-full max-w-[960px] z-10 flex flex-col gap-12 my-12 transition-all duration-500 ease-in-out">
            <div className="text-center space-y-6">
              <h1 className="font-display-lg text-5xl md:text-7xl text-primary drop-shadow-lg tracking-tight">Find Your Flavor</h1>
              <p className="font-body-lg text-lg md:text-xl text-on-surface-variant max-w-2xl mx-auto leading-relaxed">
                Let our AI curate the perfect dining experience tailored exactly to your mood and taste right now.
              </p>
            </div>

            {/* Form Box */}
            <div className="glass-surface rounded-[32px] p-8 md:p-12 transition-all duration-300">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* City Selection */}
                <div className="flex flex-col gap-3">
                  <label className="font-label-md text-label-md text-on-surface ml-1">City</label>
                  <div className="relative input-glow rounded-xl border border-outline-variant bg-surface-container-highest transition-all duration-200 shadow-inner">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant">location_on</span>
                    <select 
                      value={selectedCity}
                      onChange={(e) => setSelectedCity(e.target.value)}
                      className="w-full pl-12 pr-4 py-4 bg-transparent font-body-md text-body-md text-on-surface appearance-none focus:outline-none cursor-pointer"
                    >
                      {cities.map(c => (
                        <option key={c} className="bg-surface text-on-surface" value={c}>{c}</option>
                      ))}
                    </select>
                    <span className="absolute right-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant pointer-events-none">expand_more</span>
                  </div>
                </div>

                {/* Cuisine Selection */}
                <div className="flex flex-col gap-3">
                  <label className="font-label-md text-label-md text-on-surface ml-1">Cuisine</label>
                  <div className="relative input-glow rounded-xl border border-outline-variant bg-surface-container-highest transition-all duration-200 shadow-inner">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant">restaurant</span>
                    <select 
                      value={selectedCuisine}
                      onChange={(e) => setSelectedCuisine(e.target.value)}
                      className="w-full pl-12 pr-4 py-4 bg-transparent font-body-md text-body-md text-on-surface appearance-none focus:outline-none cursor-pointer"
                    >
                      {cuisines.map(c => (
                        <option key={c} className="bg-surface text-on-surface" value={c}>{c === 'Any Cuisine' ? 'Any Cuisine' : c}</option>
                      ))}
                    </select>
                    <span className="absolute right-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant pointer-events-none">expand_more</span>
                  </div>
                </div>
              </div>

              {/* Budget & Ratings row */}
              <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Budget Selection */}
                <div className="flex flex-col gap-3">
                  <label className="font-label-md text-label-md text-on-surface ml-1">Budget</label>
                  <div className="flex gap-3 h-[56px]">
                    <button 
                      onClick={() => setBudget('LOW')}
                      className={`flex-1 rounded-xl border font-label-md text-label-md transition-all duration-200 shadow-sm ${budget === 'LOW' ? 'border-primary bg-primary/10 text-primary' : 'border-outline-variant bg-surface-container-highest text-on-surface-variant hover:border-primary hover:text-primary'}`}
                    >
                      ₹
                    </button>
                    <button 
                      onClick={() => setBudget('MEDIUM')}
                      className={`flex-1 rounded-xl border font-label-md text-label-md transition-all duration-200 shadow-sm ${budget === 'MEDIUM' ? 'border-primary bg-primary/10 text-primary' : 'border-outline-variant bg-surface-container-highest text-on-surface-variant hover:border-primary hover:text-primary'}`}
                    >
                      ₹₹
                    </button>
                    <button 
                      onClick={() => setBudget('HIGH')}
                      className={`flex-1 rounded-xl border font-label-md text-label-md transition-all duration-200 shadow-sm ${budget === 'HIGH' ? 'border-primary bg-primary/10 text-primary' : 'border-outline-variant bg-surface-container-highest text-on-surface-variant hover:border-primary hover:text-primary'}`}
                    >
                      ₹₹₹
                    </button>
                  </div>
                </div>

                {/* Rating selection slider */}
                <div className="flex flex-col gap-3 justify-center">
                  <div className="flex justify-between items-center ml-1">
                    <label className="font-label-md text-label-md text-on-surface">Min Rating</label>
                    <span className="font-label-md text-label-md text-tertiary flex items-center gap-1">
                      <span className="material-symbols-outlined text-[18px]" style={{ fontVariationSettings: "'FILL' 1" }}>star</span> 
                      {minRating.toFixed(1)}+
                    </span>
                  </div>
                  <div className="pt-3">
                    <input 
                      type="range"
                      min="3.0"
                      max="5.0"
                      step="0.1"
                      value={minRating}
                      onChange={(e) => setMinRating(parseFloat(e.target.value))}
                      className="w-full h-2.5 bg-surface-container-highest rounded-lg appearance-none cursor-pointer accent-tertiary shadow-inner"
                    />
                  </div>
                </div>
              </div>

              {/* Vibe and description box */}
              <div className="mt-8 flex flex-col gap-3">
                <label className="font-label-md text-label-md text-on-surface ml-1">Vibe &amp; Preferences</label>
                <div className="relative input-glow rounded-xl border border-outline-variant bg-surface-container-highest transition-all duration-200 shadow-inner">
                  <textarea 
                    value={additionalPreferences}
                    onChange={(e) => setAdditionalPreferences(e.target.value)}
                    className="w-full p-5 bg-transparent font-body-md text-body-md text-on-surface resize-none focus:outline-none placeholder:text-on-surface-variant/40"
                    placeholder="e.g., 'Quiet ambiance for a date', 'Rooftop with a view', 'Pet friendly'" 
                    rows={3}
                  />
                </div>
              </div>

              {/* Action Button */}
              <div className="mt-10">
                <button 
                  onClick={handleCurate}
                  className="w-full py-5 rounded-xl bg-primary text-on-primary font-headline-md text-[20px] font-bold hover:scale-[1.01] hover:shadow-[0_15px_30px_rgba(255,179,177,0.25)] transition-all duration-300 flex items-center justify-center gap-3"
                >
                  <span className="material-symbols-outlined text-[24px]">auto_awesome</span>
                  Curate Experience
                </button>
              </div>
            </div>

            {/* Quick Tag Recommendations */}
            <div className="flex flex-wrap justify-center gap-3 mt-6 opacity-80">
              <button 
                onClick={() => handleQuickTagClick('Quiet ambiance for a date night')} 
                className="px-5 py-2 rounded-full bg-white/5 text-white/90 font-caption text-sm border border-white/10 cursor-pointer hover:bg-white/10 transition-colors backdrop-blur-sm"
              >
                Trending Tonight 💖
              </button>
              <button 
                onClick={() => handleQuickTagClick('Rooftop dining with a beautiful skyline view')} 
                className="px-5 py-2 rounded-full bg-white/5 text-white/90 font-caption text-sm border border-white/10 cursor-pointer hover:bg-white/10 transition-colors backdrop-blur-sm"
              >
                Rooftop Views 🌃
              </button>
              <button 
                onClick={() => handleQuickTagClick('Family friendly and great desserts')} 
                className="px-5 py-2 rounded-full bg-white/5 text-white/90 font-caption text-sm border border-white/10 cursor-pointer hover:bg-white/10 transition-colors backdrop-blur-sm"
              >
                Award Winning 🏆
              </button>
            </div>
          </div>
        )}

        {/* VIEW 2: LOADING SCREEN */}
        {view === 'loading' && (
          <section className="flex-1 flex flex-col px-4 py-8 gap-12 transition-opacity duration-500 ease-in-out w-full max-w-6xl justify-center items-center z-10">
            <div className="flex flex-col items-center justify-center py-12 gap-6">
              <div className="relative w-20 h-20 flex items-center justify-center">
                <div className="absolute inset-0 rounded-full border-4 border-primary-container/20 border-t-primary-container animate-spin"></div>
                <span className="material-symbols-outlined text-primary-container text-4xl animate-pulse" style={{ fontVariationSettings: "'FILL' 1" }}>smart_toy</span>
              </div>
              <h2 className="font-headline-md text-3xl text-on-surface text-center font-bold tracking-tight animate-pulse">
                AI is curating your perfect evening...
              </h2>
              <p className="font-body-md text-on-surface-variant text-center max-w-md text-lg">
                Analyzing candidates in {selectedCity} to balance high ratings with your "{selectedCuisine === 'Any Cuisine' ? 'any cuisine' : selectedCuisine}" preference.
              </p>
            </div>
            
            {/* Shimmer skeleton grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 w-full">
              <div className="bg-surface-container rounded-[24px] p-6 flex flex-col gap-4 animate-shimmer border border-white/5 h-[460px]">
                <div className="w-full h-56 bg-white/5 rounded-xl"></div>
                <div className="w-3/4 h-8 bg-white/5 rounded-md mt-2"></div>
                <div className="w-1/2 h-6 bg-white/5 rounded-md"></div>
                <div className="mt-auto w-full h-24 bg-white/5 rounded-xl"></div>
              </div>
              <div className="bg-surface-container rounded-[24px] p-6 flex flex-col gap-4 animate-shimmer border border-white/5 h-[460px]">
                <div className="w-full h-56 bg-white/5 rounded-xl"></div>
                <div className="w-2/3 h-8 bg-white/5 rounded-md mt-2"></div>
                <div className="w-1/3 h-6 bg-white/5 rounded-md"></div>
                <div className="mt-auto w-full h-24 bg-white/5 rounded-xl"></div>
              </div>
              <div className="bg-surface-container rounded-[24px] p-6 flex flex-col gap-4 animate-shimmer border border-white/5 h-[460px] hidden lg:flex">
                <div className="w-full h-56 bg-white/5 rounded-xl"></div>
                <div className="w-3/4 h-8 bg-white/5 rounded-md mt-2"></div>
                <div className="w-1/2 h-6 bg-white/5 rounded-md"></div>
                <div className="mt-auto w-full h-24 bg-white/5 rounded-xl"></div>
              </div>
            </div>
          </section>
        )}

        {/* VIEW 3: RESULTS SCREEN */}
        {view === 'results' && (
          <section className="flex-1 flex flex-col pb-12 w-full max-w-7xl mx-auto z-10 transition-all duration-700 ease-in-out">
            
            {/* Header AI Summary Box */}
            <div className="py-6 flex justify-center w-full">
              <div className="glass-panel rounded-2xl p-6 md:p-8 flex flex-col sm:flex-row items-start sm:items-center gap-6 relative overflow-hidden w-full max-w-4xl shadow-lg border border-white/10">
                <div className="absolute -left-10 -top-10 w-48 h-48 bg-primary-container/20 rounded-full blur-[50px]"></div>
                <div className="absolute right-0 bottom-0 w-32 h-32 bg-tertiary/10 rounded-full blur-[40px]"></div>
                
                <div className="w-14 h-14 md:w-16 md:h-16 rounded-full bg-surface-bright flex items-center justify-center border border-white/10 shrink-0 z-10 shadow-inner">
                  <span className="material-symbols-outlined text-tertiary md:text-[32px] text-2xl" style={{ fontVariationSettings: "'FILL' 1" }}>auto_awesome</span>
                </div>
                
                <div className="z-10 flex-1">
                  <div className="flex items-center gap-2 mb-1.5">
                    <span className="w-2.5 h-2.5 rounded-full bg-primary-container animate-pulse"></span>
                    <span className="font-label-md text-[11px] uppercase tracking-widest text-primary-container font-semibold">AI Search Complete</span>
                    {isDemoMode && (
                      <span className="text-[10px] bg-white/10 border border-white/10 text-tertiary px-2 py-0.5 rounded font-mono">Demo Mode</span>
                    )}
                  </div>
                  <p className="font-body-lg md:text-[20px] md:leading-[28px] text-body-lg text-on-surface font-semibold">
                    {summary}
                  </p>
                  <p className="font-caption text-caption md:text-[14px] text-on-surface-variant mt-2">
                    Based on {selectedCuisine === 'Any Cuisine' ? 'Any cuisine' : selectedCuisine} with rating {minRating.toFixed(1)}+ and {budget.toLowerCase()} budget in {selectedCity}.
                  </p>
                </div>
              </div>
            </div>

            {/* Masonry Bento Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mt-6">
              {recommendations.length === 0 ? (
                <div className="col-span-full py-16 text-center glass-panel rounded-3xl p-12">
                  <span className="material-symbols-outlined text-5xl text-on-surface-variant/40">sentiment_dissatisfied</span>
                  <h3 className="font-headline-md text-2xl mt-4 text-on-surface font-semibold">No matches found</h3>
                  <p className="font-body-md text-on-surface-variant max-w-md mx-auto mt-2">
                    We couldn't find restaurants matching all filters. Try lowering your minimum rating or broadening your cuisine request.
                  </p>
                  <button 
                    onClick={() => setView('search')}
                    className="mt-6 px-6 py-3 bg-primary text-on-primary rounded-xl font-label-md hover:scale-[1.02] transition-transform"
                  >
                    Adjust Filters
                  </button>
                </div>
              ) : (
                recommendations.map((rec, index) => (
                  <article 
                    key={rec.restaurantId || index} 
                    className="card-hover-lift bg-surface-container rounded-3xl overflow-hidden border border-white/5 flex flex-col relative group shadow-lg"
                  >
                    {/* Top image section */}
                    <div className="img-zoom-wrapper relative h-[250px] w-full">
                      <img 
                        alt={rec.name}
                        className="w-full h-full object-cover" 
                        src={getCuisineImage(rec.cuisines)} 
                      />
                      {/* Dark overlay */}
                      <div className="absolute inset-0 bg-gradient-to-t from-surface-container via-surface-container/20 to-transparent"></div>
                      
                      {/* Rating Badges */}
                      <div className="absolute top-5 left-5 flex gap-2">
                        <span className="bg-surface/90 backdrop-blur-md px-4 py-1.5 rounded-full font-caption text-[14px] font-bold text-on-surface border border-white/10 shadow-sm flex items-center gap-1">
                          <span className="material-symbols-outlined text-tertiary text-[16px]" style={{ fontVariationSettings: "'FILL' 1" }}>star</span> 
                          {typeof rec.rating === 'number' ? rec.rating.toFixed(1) : parseFloat(rec.rating).toFixed(1)}
                        </span>
                      </div>
                      <div className="absolute top-5 right-5">
                        <button className="w-10 h-10 rounded-full bg-surface/80 backdrop-blur-md flex items-center justify-center border border-white/10 hover:bg-white/10 text-on-surface transition-colors">
                          <span className="material-symbols-outlined text-[20px]" style={{ fontVariationSettings: "'FILL' 0" }}>bookmark</span>
                        </button>
                      </div>
                    </div>

                    {/* Bottom description section */}
                    <div className="p-6 md:p-8 flex flex-col flex-1 gap-4 relative z-10 -mt-8">
                      <div className="flex justify-between items-end gap-2">
                        <h3 className="font-headline-lg-mobile text-[22px] md:text-[24px] font-extrabold text-on-surface tracking-tight leading-tight">
                          {rec.name}
                        </h3>
                        <span className="font-label-md text-[14px] font-bold text-on-surface-variant bg-surface-bright/80 backdrop-blur-md px-3 py-1.5 rounded-lg border border-white/10 shrink-0 shadow-sm">
                          ₹{rec.costForTwo} for 2
                        </span>
                      </div>

                      {/* Cuisines / tags */}
                      <div className="flex flex-wrap gap-2 mt-1">
                        {rec.cuisines && rec.cuisines.map((c, i) => (
                          <span key={i} className="px-3.5 py-1.5 bg-white/5 border border-white/10 rounded-full font-caption text-[12px] text-white/90">
                            {c}
                          </span>
                        ))}
                      </div>

                      <div className="flex-1"></div> {/* Spacer */}

                      {/* AI Explanations */}
                      <div className="mt-4 glass-panel rounded-2xl p-5 ai-pulse-border relative overflow-hidden bg-primary-container/5 border-primary-container/20">
                        <div className="absolute -right-4 -bottom-4 w-24 h-24 bg-primary-container/20 rounded-full blur-[25px]"></div>
                        <div className="flex items-start gap-3.5 relative z-10">
                          <span className="material-symbols-outlined text-primary-container text-[22px] mt-0.5 drop-shadow-md" style={{ fontVariationSettings: "'FILL' 1" }}>temp_preferences_custom</span>
                          <p className="font-body-md text-on-surface/90 text-[14.5px] leading-relaxed">
                            {rec.explanation || "A curated recommendation matching your preferred location and criteria perfectly."}
                          </p>
                        </div>
                      </div>

                      <button className="mt-6 w-full py-4 bg-primary-container text-white font-label-md text-[15px] font-bold rounded-xl hover:scale-[1.02] hover:shadow-[0_10px_20px_rgba(255,83,90,0.3)] hover:bg-primary-container/90 transition-all duration-200 shadow-lg">
                        Book Table
                      </button>
                    </div>
                  </article>
                ))
              )}
            </div>

            {/* Back action trigger */}
            <div className="mt-12 flex justify-center w-full">
              <button 
                onClick={() => setView('search')}
                className="px-8 py-4 rounded-xl border border-white/10 bg-white/5 text-on-surface font-label-md text-[16px] hover:bg-white/10 transition-colors flex items-center gap-2"
              >
                <span className="material-symbols-outlined">arrow_back</span>
                Search Again
              </button>
            </div>
          </section>
        )}
      </main>

      {/* Bottom navbar for mobile viewport sizes */}
      <nav className="md:hidden fixed bottom-0 left-0 w-full flex justify-around items-center pt-2 pb-6 px-4 bg-surface/85 backdrop-blur-xl dark:bg-surface-container-lowest/85 border-t border-white/5 shadow-[0_-10px_30px_rgba(0,0,0,0.5)] z-50 rounded-t-xl">
        <div 
          onClick={() => setView('search')}
          className={`flex flex-col items-center justify-center transition-colors cursor-pointer w-[64px] ${view === 'search' ? 'text-primary-container' : 'text-secondary dark:text-secondary-fixed-dim opacity-60 hover:opacity-100'}`}
        >
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: view === 'search' ? "'FILL' 1" : "'FILL' 0" }}>explore</span>
          <span className="font-label-md text-[10px]">Discover</span>
        </div>
        <div 
          onClick={() => {
            if (recommendations.length > 0) setView('results');
          }}
          className={`flex flex-col items-center justify-center cursor-pointer w-[64px] relative ${view === 'results' ? 'text-primary-container scale-110' : 'text-secondary dark:text-secondary-fixed-dim opacity-60 hover:opacity-100'} ${recommendations.length === 0 ? 'opacity-30 pointer-events-none' : ''}`}
        >
          {view === 'results' && (
            <div className="absolute -top-2 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-primary-container rounded-full blur-[1px]"></div>
          )}
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 1" }}>smart_toy</span>
          <span className="font-label-md text-[10px] font-bold">AI Chat</span>
        </div>
        <div className="flex flex-col items-center justify-center text-secondary dark:text-secondary-fixed-dim opacity-60 hover:text-primary transition-colors cursor-pointer w-[64px]">
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 0" }}>bookmark</span>
          <span className="font-label-md text-[10px]">Saved</span>
        </div>
        <div className="flex flex-col items-center justify-center text-secondary dark:text-secondary-fixed-dim opacity-60 hover:text-primary transition-colors cursor-pointer w-[64px]">
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 0" }}>person</span>
          <span className="font-label-md text-[10px]">Profile</span>
        </div>
      </nav>
    </>
  );
}
